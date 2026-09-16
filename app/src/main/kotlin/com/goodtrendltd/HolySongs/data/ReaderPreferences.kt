package com.goodtrendltd.HolySongs.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Small adapter for the existing reader preference file.
 *
 * Reading is deliberately owned by the collection coroutine: constructing this adapter does not
 * open the preference file or perform disk work. Observation never repairs or normalizes stored
 * values; the effective font size is only a display-time safety value.
 */
class ReaderPreferences(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val applicationContext: Context = context.applicationContext

    val snapshots: Flow<ReaderPreferenceSnapshot> = flow {
        val preferences = applicationContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)

        fun readSnapshot(): ReaderPreferenceSnapshot {
            val values = preferences.all
            val rawFontSize = values[FONT_SIZE_KEY] as? Int ?: DEFAULT_FONT_SIZE
            val nightMode = values[NIGHT_MODE_KEY] as? Boolean ?: DEFAULT_NIGHT_MODE
            return ReaderPreferenceSnapshot(
                fontSize = rawFontSize,
                effectiveFontSize = rawFontSize.takeIf { it in MIN_EFFECTIVE_FONT_SIZE..MAX_EFFECTIVE_FONT_SIZE }
                    ?: DEFAULT_FONT_SIZE,
                nightMode = nightMode,
            )
        }

        // Android delivers preference callbacks on main. Queue refresh requests, not snapshot
        // jobs: one collector reads/emits serially, so an older read cannot overtake a newer one.
        val changes = Channel<Unit>(Channel.CONFLATED)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            changes.trySend(Unit)
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        try {
            changes.trySend(Unit)
            for (ignored in changes) emit(readSnapshot())
        } finally {
            // No suspension between registration and entering this try/finally. Cancellation
            // during the initial read/emission is covered just like a later subscription stop.
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
            changes.close()
        }
    }.flowOn(dispatcher).distinctUntilChanged()

    /** Persist one of the eight existing settings choices; invalid values are ignored. */
    fun setFontSize(value: Int) {
        if (value !in FONT_SIZE_CHOICES) return
        preferences().edit().putInt(FONT_SIZE_KEY, value).apply()
    }

    /** Persist the existing reset action without touching theme or unrelated keys. */
    fun resetFontSize() {
        preferences().edit().putInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE).apply()
    }

    fun setNightMode(value: Boolean) {
        preferences().edit().putBoolean(NIGHT_MODE_KEY, value).apply()
    }

    private fun preferences(): SharedPreferences =
        applicationContext.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)

    private companion object {
        const val PREFERENCE_FILE = "appPrefFile"
        const val FONT_SIZE_KEY = "fontSize"
        const val NIGHT_MODE_KEY = "nightMode"
        const val DEFAULT_FONT_SIZE = 20
        const val DEFAULT_NIGHT_MODE = true
        const val MIN_EFFECTIVE_FONT_SIZE = 1
        const val MAX_EFFECTIVE_FONT_SIZE = 200
        val FONT_SIZE_CHOICES = setOf(16, 18, 20, 22, 24, 26, 28, 30)
    }
}

data class ReaderPreferenceSnapshot(
    val fontSize: Int,
    val effectiveFontSize: Int,
    val nightMode: Boolean,
)
