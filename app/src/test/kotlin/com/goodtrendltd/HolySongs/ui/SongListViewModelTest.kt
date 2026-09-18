package com.goodtrendltd.HolySongs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import com.goodtrendltd.HolySongs.data.StoredSong
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
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
    private val validRows = listOf(
        StoredSong("云上太阳", "太阳lyric", 0),
        StoredSong("爱我", "爱lyric", 1),
    )

    private class Owner : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    private data class Created(
        val owner: Owner,
        val provider: ViewModelProvider,
        val model: SongListViewModel,
    )

    private fun create(loader: SongCatalogLoader): Created {
        val owner = Owner()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(SongListViewModel::class.java))
                return SongListViewModel(loader) as T
            }
        }
        val provider = ViewModelProvider(owner, factory)
        return Created(owner, provider, provider[SongListViewModel::class.java])
    }

    private fun loader(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        source: () -> List<StoredSong>,
        opened: AtomicInteger,
    ) = SongCatalogLoader(
        readSongs = {
            opened.incrementAndGet()
            source()
        },
        dispatcher = dispatcher,
    )

    @Test
    fun publishesLoadingThenReadyAndLoadsOncePerViewModelOwner() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            val created = create(loader(worker, { validRows }, opened))
            stores += created.owner.viewModelStore
            assertTrue(created.model.state.value is SongCatalogUiState.Loading)
            assertSame(created.model, created.provider[SongListViewModel::class.java])

            advanceUntilIdle()
            val ready = created.model.state.value as SongCatalogUiState.Ready
            assertEquals(setOf("云上太阳", "爱我"), ready.catalog.lyricsByTitle.keys)
            assertEquals(1, opened.get())

            val first = backgroundScope.launch { created.model.state.collect {} }
            val second = backgroundScope.launch { created.model.state.collect {} }
            advanceUntilIdle()
            first.cancel()
            second.cancel()
            created.model.retry()
            advanceUntilIdle()
            assertEquals(1, opened.get())
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun searchSessionStartsInactiveAndRetainsRawQueryWithoutReloading() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            val created = create(loader(worker, { validRows }, opened))
            stores += created.owner.viewModelStore
            advanceUntilIdle()
            assertEquals(SearchSession(), created.model.searchSession.value)

            created.model.enterSearch()
            assertEquals(SearchSession(isActive = true), created.model.searchSession.value)
            created.model.updateSearchQuery("  爱 A  ")
            assertEquals(SearchSession(isActive = true, query = "  爱 A  "), created.model.searchSession.value)
            advanceUntilIdle()
            assertEquals(1, opened.get())
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun clearExitAndReopenMaintainSearchSessionInvariants() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val created = create(loader(worker, { validRows }, AtomicInteger()))
            stores += created.owner.viewModelStore
            advanceUntilIdle()

            created.model.enterSearch()
            created.model.updateSearchQuery("爱")
            created.model.clearSearchQuery()
            assertEquals(SearchSession(isActive = true), created.model.searchSession.value)
            created.model.clearSearchQuery()
            assertEquals(SearchSession(isActive = true), created.model.searchSession.value)

            created.model.exitSearch()
            assertEquals(SearchSession(), created.model.searchSession.value)
            created.model.exitSearch()
            assertEquals(SearchSession(), created.model.searchSession.value)
            created.model.enterSearch()
            assertEquals(SearchSession(isActive = true), created.model.searchSession.value)
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun queryUpdatesAreIgnoredWhileSearchIsInactive() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val created = create(loader(worker, { validRows }, AtomicInteger()))
            stores += created.owner.viewModelStore
            advanceUntilIdle()
            created.model.updateSearchQuery("stale")
            created.model.clearSearchQuery()
            assertEquals(SearchSession(), created.model.searchSession.value)
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun errorRetriesOnlyAfterExplicitRequest() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val opened = AtomicInteger()
            var rows: List<StoredSong> = listOf(StoredSong("", "invalid", 0))
            val created = create(loader(worker, { rows }, opened))
            stores += created.owner.viewModelStore
            advanceUntilIdle()
            assertTrue(created.model.state.value is SongCatalogUiState.Error)

            rows = validRows
            created.model.retry()
            assertTrue(created.model.state.value is SongCatalogUiState.Loading)
            advanceUntilIdle()
            assertTrue(created.model.state.value is SongCatalogUiState.Ready)
            assertEquals(2, opened.get())
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun cancellationIsNotPublishedAsError() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val created = create(
                SongCatalogLoader(
                    readSongs = { throw CancellationException("cancelled") },
                    dispatcher = worker,
                )
            )
            stores += created.owner.viewModelStore
            advanceUntilIdle()
            assertTrue(created.model.state.value is SongCatalogUiState.Loading)
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun clearingOwnerBeforeQueuedLoadPreventsObsoletePublication() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val worker = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(main)
        val stores = mutableListOf<ViewModelStore>()
        try {
            val first = create(loader(worker, { validRows }, AtomicInteger()))
            stores += first.owner.viewModelStore
            first.owner.viewModelStore.clear()
            advanceUntilIdle()
            assertTrue(first.model.state.value is SongCatalogUiState.Loading)

            val second = create(loader(worker, { validRows }, AtomicInteger()))
            stores += second.owner.viewModelStore
            advanceUntilIdle()
            assertTrue(second.model.state.value is SongCatalogUiState.Ready)
            assertNotSame(first.model, second.model)
        } finally {
            stores.forEach(ViewModelStore::clear)
            Dispatchers.resetMain()
        }
    }
}
