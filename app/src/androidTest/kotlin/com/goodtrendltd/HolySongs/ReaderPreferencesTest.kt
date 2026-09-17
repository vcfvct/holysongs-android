package com.goodtrendltd.HolySongs

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goodtrendltd.HolySongs.data.ReaderPreferences
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Real storage/Flow tests. Run only with -e readerPreferencesMutate true on the disposable AVD. */
@RunWith(AndroidJUnit4::class)
class ReaderPreferencesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext.applicationContext
    private val font = "fontSize"
    private val night = "nightMode"

    @Test
    fun missingAndWrongTypeValuesAreDisplayOnlyDefaults() = withFixture { f ->
        f.seed(null, null)
        val adapter = f.createOnMain()
        val observer = f.observe(adapter)
        observer.await { it.fontSize == 20 && it.effectiveFontSize == 20 && it.nightMode }
        assertFalse(f.prefs.contains(font))
        assertFalse(f.prefs.contains(night))
        assertEquals(0, f.tracker.edits.get())

        f.seed("24", "wrongType")
        // The effective snapshot may be unchanged, so restart observation to force a fresh read
        // instead of demanding a duplicate emission from a legitimate distinctUntilChanged Flow.
        observer.stop()
        val before = snapshot(f.prefs)
        f.observe(adapter).await { it.fontSize == 20 && it.effectiveFontSize == 20 && it.nightMode }
        assertEquals(before, snapshot(f.prefs))
        assertEquals("24", f.prefs.all[font])
        assertEquals("wrongType", f.prefs.all[night])
        assertEquals(0, f.tracker.edits.get())
    }

    @Test
    fun everyFontBoundaryPreservesItsRawTypeAndValue() = withFixture { f ->
        val adapter = f.createOnMain()
        val cases: List<Any?> = listOf(null, "24", Int.MIN_VALUE, -1, 0, 1, 15, 16,
            20, 30, 32, 200, 201, Int.MAX_VALUE)
        for (raw in cases) {
            f.seed(raw, true)
            val before = snapshot(f.prefs)
            val expectedRaw = raw as? Int ?: 20
            val effective = expectedRaw.takeIf { it in 1..200 } ?: 20
            val observer = f.observe(adapter)
            try {
                observer.await { it.fontSize == expectedRaw && it.effectiveFontSize == effective && it.nightMode }
                assertEquals("Read must not repair any raw field", before, snapshot(f.prefs))
                assertFalse(f.prefs.contains("effectiveFontSize"))
            } finally {
                observer.stop()
            }
        }
        assertEquals(0, f.tracker.edits.get())
        assertEquals(f.tracker.registers.get(), f.tracker.unregisters.get())
    }

    @Test
    fun explicitChoicesAndResetAreSparseWrites() = withFixture { f ->
        f.seed(32, false)
        val sentinel = "testOnlyUnrelatedPreference"
        assertFalse("Fixture sentinel must not overwrite existing data", f.prefs.contains(sentinel))
        assertTrue(f.prefs.edit().putString(sentinel, "keep").commit())
        val untouched = snapshot(f.prefs).filterKeys { it != font && it != night }
        val adapter = f.createOnMain()
        val observer = f.observe(adapter)
        observer.await { it.fontSize == 32 && it.effectiveFontSize == 32 && !it.nightMode }
        assertEquals(0, f.tracker.edits.get())

        for (value in listOf(16, 18, 20, 22, 24, 26, 28, 30)) {
            onMain { adapter.setFontSize(value) }
            observer.await { it.fontSize == value && it.effectiveFontSize == value && !it.nightMode }
            assertEquals(value, f.prefs.all[font])
            assertEquals(false, f.prefs.all[night])
            assertEquals(untouched, snapshot(f.prefs).filterKeys { it != font && it != night })
        }
        onMain { adapter.resetFontSize() }
        observer.await { it.fontSize == 20 && it.effectiveFontSize == 20 && !it.nightMode }
        assertEquals(false, f.prefs.all[night])

        onMain { adapter.setNightMode(true) }
        observer.await { it.fontSize == 20 && it.nightMode }
        assertEquals(20, f.prefs.all[font])
        onMain { adapter.setNightMode(false) }
        observer.await { !it.nightMode }
        assertEquals(untouched, snapshot(f.prefs).filterKeys { it != font && it != night })

        val beforeInvalid = snapshot(f.prefs)
        for (invalid in listOf(Int.MIN_VALUE, 0, 1, 15, 17, 31, 32, 200, Int.MAX_VALUE)) {
            // Invalid explicit choices may be rejected or ignored; they must never be persisted.
            onMain {
                try { adapter.setFontSize(invalid) } catch (_: IllegalArgumentException) { }
            }
            assertEquals(beforeInvalid, snapshot(f.prefs))
        }
    }

    @Test
    fun listenerRegistersBeforeSnapshotPairsCleanupAndRefreshesSameAdapter() = withFixture { f ->
        f.seed(20, true)
        val adapter = f.createOnMain()
        val first = f.observe(adapter)
        first.await { it.fontSize == 20 && it.nightMode }
        val events = synchronized(f.tracker.events) { f.tracker.events.toList() }
        assertTrue("Listener precedes fresh getAll", events.indexOf("register") >= 0 &&
            events.indexOf("read") > events.indexOf("register"))
        assertEquals(1, f.tracker.registers.get())
        assertFalse("Disk-backed reads cannot run on Android main", f.tracker.readOnMain)
        assertFalse("Storage initialization cannot run on Android main", f.tracker.openOnMain)
        assertEquals(0, f.tracker.edits.get())

        // Actual external legacy storage writes are delivered by Android, not a fake callback.
        assertTrue(f.prefs.edit().putInt(font, 16).putBoolean(night, false).commit())
        first.await { it.fontSize == 16 && !it.nightMode }
        first.stop()
        assertEquals(1, f.tracker.unregisters.get())
        assertTrue(f.prefs.edit().putInt(font, 30).commit())
        val second = f.observe(adapter)
        second.await { it.fontSize == 30 && it.effectiveFontSize == 30 && !it.nightMode }
        assertEquals(2, f.tracker.registers.get())
        second.stop()
        assertEquals(2, f.tracker.unregisters.get())
        assertEquals(0, f.tracker.edits.get())
        // Re-subscription/reopening is not proof of process-death or durable relaunch persistence.
    }

    private fun withFixture(block: suspend CoroutineScope.(Fixture) -> Unit) = runBlocking {
        assumeTrue("Pass -e readerPreferencesMutate true on the disposable target",
            InstrumentationRegistry.getArguments().getString("readerPreferencesMutate") == "true")
        assertEquals("com.goodtrendltd.HolySongs", app.packageName)
        assertEquals("appPrefFile", app.getString(R.string.app_pref))
        assertEquals(font, app.getString(R.string.font_size_pref_key))
        assertEquals(night, app.getString(R.string.night_mode_pref_key))
        val prefs = app.getSharedPreferences("appPrefFile", Context.MODE_PRIVATE)
        val original = snapshot(prefs)
        val fixture = Fixture(prefs, this)
        try {
            block(fixture)
        } finally {
            withContext(NonCancellable) {
                try {
                    fixture.jobs.asReversed().forEach { it.cancelAndJoin() }
                } finally {
                    // Remove test-created keys as well as restore original presence/types/values.
                    val editor = prefs.edit()
                    prefs.all.keys.filter { it !in original }.forEach { editor.remove(it) }
                    original.forEach { (key, value) -> putTyped(editor, key, value) }
                    assertTrue("Restore must persist", editor.commit())
                    assertEquals("Exact original private state restored", original, snapshot(prefs))
                }
            }
        }
    }

    private inner class Fixture(val prefs: SharedPreferences, private val scope: CoroutineScope) {
        val tracker = Tracker()
        val jobs = mutableListOf<Job>()

        fun seed(fontValue: Any?, nightValue: Any?) {
            val editor = prefs.edit()
            putTyped(editor, font, fontValue)
            putTyped(editor, night, nightValue)
            assertTrue(editor.commit())
        }

        fun createOnMain(): ReaderPreferences {
            val context = object : ContextWrapper(app) {
                override fun getApplicationContext(): Context = this
                override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
                    check(name == "appPrefFile") { "Unexpected preference store" }
                    if (Looper.myLooper() == Looper.getMainLooper()) tracker.openOnMain = true
                    return object : SharedPreferences by prefs {
                        override fun getAll(): MutableMap<String, *> {
                            tracker.events += "read"
                            if (Looper.myLooper() == Looper.getMainLooper()) tracker.readOnMain = true
                            return HashMap(prefs.all)
                        }
                        override fun edit(): SharedPreferences.Editor {
                            tracker.edits.incrementAndGet()
                            return prefs.edit()
                        }
                        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
                            tracker.events += "register"
                            tracker.registers.incrementAndGet()
                            prefs.registerOnSharedPreferenceChangeListener(listener)
                        }
                        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
                            tracker.unregisters.incrementAndGet()
                            prefs.unregisterOnSharedPreferenceChangeListener(listener)
                        }
                    }
                }
            }
            var result: ReaderPreferences? = null
            onMain { result = ReaderPreferences(context, Dispatchers.IO) }
            return checkNotNull(result)
        }

        fun observe(adapter: ReaderPreferences): Observer {
            val values = Channel<ReaderPreferenceSnapshot>(Channel.UNLIMITED)
            // Start collection where a real UI would. Await real asynchronous emissions rather
            // than draining a different test scheduler while Android/IO callbacks are pending.
            val job = scope.launch(Dispatchers.Main) {
                adapter.snapshots.collect { values.send(it) }
            }
            jobs += job
            return Observer(job, values)
        }
    }

    private class Observer(val job: Job, private val values: Channel<ReaderPreferenceSnapshot>) {
        suspend fun await(matches: (ReaderPreferenceSnapshot) -> Boolean): ReaderPreferenceSnapshot =
            withTimeout(5_000) {
                var value = values.receive()
                while (!matches(value)) value = values.receive()
                value
            }
        suspend fun stop() { job.cancelAndJoin() }
    }

    private class Tracker {
        val registers = AtomicInteger()
        val unregisters = AtomicInteger()
        val edits = AtomicInteger()
        val events = Collections.synchronizedList(mutableListOf<String>())
        @Volatile var readOnMain = false
        @Volatile var openOnMain = false
    }

    private fun onMain(block: () -> Unit) {
        var error: Throwable? = null
        instrumentation.runOnMainSync { try { block() } catch (t: Throwable) { error = t } }
        error?.let { throw it }
        instrumentation.waitForIdleSync()
    }

    private fun snapshot(prefs: SharedPreferences): Map<String, Any?> =
        prefs.all.mapValues { (_, v) -> if (v is Set<*>) v.toSet() else v }

    private fun putTyped(editor: SharedPreferences.Editor, key: String, value: Any?) {
        when (value) {
            null -> editor.remove(key)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            else -> error("Unsupported private preference type")
        }
    }
}
