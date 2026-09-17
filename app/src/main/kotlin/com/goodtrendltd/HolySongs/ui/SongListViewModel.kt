package com.goodtrendltd.HolySongs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodtrendltd.HolySongs.data.SongCatalog
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/** Owns one catalog load for the Activity/ViewModel owner. */
class SongListViewModel(
    private val loader: SongCatalogLoader,
) : ViewModel() {
    private val mutableState = MutableStateFlow<SongCatalogUiState>(SongCatalogUiState.Loading)
    val state: StateFlow<SongCatalogUiState> = mutableState.asStateFlow()

    init {
        load()
    }

    /** Starts a new load only after a failed load has explicitly been retried. */
    fun retry() {
        if (mutableState.value !is SongCatalogUiState.Error) return
        mutableState.value = SongCatalogUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val catalog = loader.load()
                // A loader may complete after owner cancellation (for example, an injected test
                // loader that ignores interruption). Never publish that obsolete result.
                coroutineContext.ensureActive()
                mutableState.value = SongCatalogUiState.Ready(catalog)
            } catch (cancellation: CancellationException) {
                // Cancellation is lifecycle control, not a user-visible loading error.
                throw cancellation
            } catch (failure: Throwable) {
                // Cancellation can race with a non-suspending failure path; only a live owner may
                // expose an error state.
                if (coroutineContext.isActive) {
                    mutableState.value = SongCatalogUiState.Error(displayableReason(failure))
                }
            }
        }
    }

    private fun displayableReason(failure: Throwable): String =
        failure.message?.takeIf { it.isNotBlank() } ?: "Unable to load songs"
}

sealed interface SongCatalogUiState {
    data object Loading : SongCatalogUiState
    data class Ready(val catalog: SongCatalog) : SongCatalogUiState
    data class Error(val reason: String) : SongCatalogUiState
}
