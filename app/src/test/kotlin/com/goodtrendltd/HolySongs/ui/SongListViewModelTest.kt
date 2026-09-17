package com.goodtrendltd.HolySongs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Deterministic owner/lifetime tests for the one-shot catalog load state machine. */
class SongListViewModelTest {
    private val validXml = "<songs>" +
        "<song><name>云上太阳</name><lyric>太阳 lyric</lyric></song>" +
        "<song><name>爱 我</name><lyric>爱 lyric</lyric></song>" +
        "</songs>"

    private class Owner : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    private class RecordingDispatcher(private val actual: kotlinx.coroutines.CoroutineDispatcher) :
        kotlinx.coroutines.CoroutineDispatcher() {
        var dispatchCount = 0
            private set

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount++
            actual.dispatch(context, block)
        }
    }

    private data class Created(val owner: Owner, val provider: ViewModelProvider, val model: SongListViewModel)

    private fun create(
        loader: SongCatalogLoader,
    ): Created {
        val owner = Owner()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (!modelClass.isAssignableFrom(SongListViewModel::class.java)) {
                    throw IllegalArgumentException("Unexpected ViewModel ${modelClass.name}")
                }
                return SongListViewModel(loader) as T
            }
        }
        val provider = ViewModelProvider(owner, factory)
        return Created(owner, provider, provider[SongListViewModel::class.java])
    }

    private fun loader(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        source: () -> String,
        opened: AtomicInteger,
    ): SongCatalogLoader = SongCatalogLoader(
        openAsset = {
            opened.incrementAndGet()
            ByteArrayInputStream(source().toByteArray(StandardCharsets.UTF_8))
        },
        dispatcher = dispatcher,
    )

    @Test
    fun publishesLoadingThenReadyAndLoadsOnceOffMainPerViewModelOwner() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = RecordingDispatcher(StandardTestDispatcher(testScheduler))
        Dispatchers.setMain(main)
        val ownedStores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            val created = create(loader(worker, { validXml }, opened))
            ownedStores += created.owner.viewModelStore
            assertTrue(created.model.state.value is SongCatalogUiState.Loading)
            val sameModel = created.provider[SongListViewModel::class.java]
            assertSame("ViewModelStore owner retains one model", created.model, sameModel)
            created.model.retry()
            assertEquals("Retry while Loading is ignored", 0, opened.get())

            advanceUntilIdle()
            val state = created.model.state.value
            assertTrue("Successful load publishes Ready", state is SongCatalogUiState.Ready)
            assertEquals(1, opened.get())
            assertTrue("Catalog work is dispatched through the injected worker", worker.dispatchCount > 0)

            // Subscriptions are observers, not load commands. Multiple subscriptions and reads
            // after completion must not start another asset read.
            val firstSubscription = backgroundScope.launch { created.model.state.collect {} }
            val secondSubscription = backgroundScope.launch { created.model.state.collect {} }
            advanceUntilIdle()
            firstSubscription.cancel()
            secondSubscription.cancel()
            assertEquals(1, opened.get())

            created.model.retry()
            advanceUntilIdle()
            assertEquals("retry is ignored outside Error", 1, opened.get())
        } finally {
            ownedStores.forEach { it.clear() }
            Dispatchers.resetMain()
        }
    }

    @Test
    fun publishesLoadingThenErrorAndRetriesOnlyAfterExplicitErrorRetry() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val ownedStores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            var source = "<songs>"
            val created = create(loader(worker, { source }, opened))
            ownedStores += created.owner.viewModelStore
            advanceUntilIdle()

            val error = created.model.state.value
            assertTrue("Malformed input reaches displayable Error", error is SongCatalogUiState.Error)
            assertTrue((error as SongCatalogUiState.Error).reason.isNotBlank())
            assertEquals(1, opened.get())

            source = validXml
            created.model.retry()
            assertTrue("Explicit retry enters Loading before work completes", created.model.state.value is SongCatalogUiState.Loading)
            advanceUntilIdle()
            assertTrue(created.model.state.value is SongCatalogUiState.Ready)
            assertEquals(2, opened.get())

            created.model.retry()
            advanceUntilIdle()
            assertEquals("Ready does not implicitly reload", 2, opened.get())
        } finally {
            ownedStores.forEach { it.clear() }
            Dispatchers.resetMain()
        }
    }

    @Test
    fun cancellationAndOwnerClearingDoNotPublishAnErrorOrAnObsoleteResult() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val ownedStores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            val cancelledLoader = SongCatalogLoader(
                openAsset = {
                    opened.incrementAndGet()
                    throw CancellationException("test cancellation")
                },
                dispatcher = worker,
            )
            val cancelled = create(cancelledLoader)
            ownedStores += cancelled.owner.viewModelStore
            advanceUntilIdle()
            assertTrue(
                "Coroutine cancellation is not a displayable parse error",
                cancelled.model.state.value is SongCatalogUiState.Loading,
            )
            assertEquals(1, opened.get())

            // Clearing before the queued load runs cancels the owner job. The old model must not
            // publish a late Ready/Error after its owner has been cleared.
            val queued = create(loader(worker, { validXml }, AtomicInteger()))
            ownedStores += queued.owner.viewModelStore
            assertTrue(queued.model.state.value is SongCatalogUiState.Loading)
            queued.owner.viewModelStore.clear()
            advanceUntilIdle()
            assertTrue(
                "An obsolete cleared-owner job cannot publish a result",
                queued.model.state.value is SongCatalogUiState.Loading,
            )
            assertNotSame("Each owner gets a distinct model", cancelled.model, queued.model)
        } finally {
            ownedStores.forEach { it.clear() }
            Dispatchers.resetMain()
        }
    }

    @Test
    fun inProgressClearingCancelsAfterReadBeginsAndClosesTheStream() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val ownedStores = mutableListOf<ViewModelStore>()
        lateinit var created: Created
        val bytes = validXml.toByteArray(StandardCharsets.UTF_8)
        val stream = CallbackShortReadingInputStream(bytes, maxChunk = 2) {
            // This callback runs from the loader's first worker read: work has begun, and all
            // subsequent reads are still allowed to drain the bytes after owner cancellation.
            created.owner.viewModelStore.clear()
        }
        try {
            val opened = AtomicInteger()
            val loader = SongCatalogLoader(
                openAsset = {
                    opened.incrementAndGet()
                    stream
                },
                dispatcher = worker,
            )
            created = create(loader)
            ownedStores += created.owner.viewModelStore
            advanceUntilIdle()

            assertEquals(1, opened.get())
            assertTrue("The cancellation callback ran after reading began", stream.started)
            assertTrue("The in-progress stream was closed", stream.closed)
            assertTrue(
                "Clearing during a read prevents obsolete Ready/Error publication",
                created.model.state.value is SongCatalogUiState.Loading,
            )
        } finally {
            ownedStores.forEach { it.clear() }
            Dispatchers.resetMain()
        }
    }

    private class CallbackShortReadingInputStream(
        bytes: ByteArray,
        private val maxChunk: Int,
        private val onFirstRead: () -> Unit,
    ) : java.io.InputStream() {
        private val delegate = ByteArrayInputStream(bytes)
        private var callbackSent = false

        var started = false
            private set
        var closed = false
            private set

        init {
            require(maxChunk in 1..3)
        }

        override fun available(): Int = 0

        override fun read(): Int {
            markStarted()
            return delegate.read()
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            markStarted()
            return delegate.read(buffer, offset, minOf(length, maxChunk))
        }

        private fun markStarted() {
            started = true
            if (!callbackSent) {
                callbackSent = true
                onFirstRead()
            }
        }

        override fun close() {
            closed = true
            delegate.close()
        }
    }
}
