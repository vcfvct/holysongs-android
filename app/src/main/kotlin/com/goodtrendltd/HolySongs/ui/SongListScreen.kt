package com.goodtrendltd.HolySongs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.testTag
import com.goodtrendltd.HolySongs.R
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import com.goodtrendltd.HolySongs.data.SongCatalog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

/** Compose list/sidebar boundary; navigation remains callback-only and Activity-owned. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    state: SongCatalogUiState,
    preferences: ReaderPreferenceSnapshot,
    onSongSelected: (title: String, lyric: String) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onShareApp: () -> Unit,
    onRetry: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var indicatorLetter by remember { mutableStateOf<String?>(null) }
    var indicatorGeneration by remember { mutableIntStateOf(0) }
    var pendingSectionScroll by remember { mutableStateOf<Job?>(null) }
    // The overflow menu is transient UI state; do not restore an open popup across recreation.
    var actionsExpanded by remember { mutableStateOf(false) }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val scope = rememberCoroutineScope()

    fun showIndicator(letter: String?) {
        if (letter != null) {
            indicatorLetter = letter
            indicatorGeneration++
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                pendingSectionScroll?.cancel()
                pendingSectionScroll = null
                indicatorLetter = null
                indicatorGeneration++
                actionsExpanded = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            pendingSectionScroll?.cancel()
            actionsExpanded = false
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val catalog = (state as? SongCatalogUiState.Ready)?.catalog
    LaunchedEffect(catalog, lifecycleOwner) {
        if (catalog == null) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }
                .drop(1)
                .distinctUntilChanged()
                .collect { (index, _) -> showIndicator(catalog.initials.getOrNull(index)) }
        }
    }
    LaunchedEffect(indicatorLetter, indicatorGeneration, lifecycleOwner) {
        if (indicatorLetter == null) return@LaunchedEffect
        val generation = indicatorGeneration
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            delay(2_000)
            if (indicatorGeneration == generation) indicatorLetter = null
        }
    }

    val moreActionsDescription = stringResource(R.string.more_actions)
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    Box {
                        IconButton(
                            onClick = { actionsExpanded = true },
                            modifier = Modifier.semantics {
                                contentDescription = moreActionsDescription
                                stateDescription = if (actionsExpanded) "已展开" else "已收起"
                            },
                        ) { Text("⋮", style = MaterialTheme.typography.titleLarge) }
                        DropdownMenu(
                            expanded = actionsExpanded,
                            onDismissRequest = { actionsExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_text)) },
                                onClick = { actionsExpanded = false; onSettings() },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about)) },
                                onClick = { actionsExpanded = false; onAbout() },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sharing_app_text)) },
                                onClick = { actionsExpanded = false; onShareApp() },
                            )
                        }
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                ),
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        when (state) {
            SongCatalogUiState.Loading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .testTag("catalog-loading"),
            )
            is SongCatalogUiState.Error -> ErrorContent(
                reason = state.reason,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .testTag("catalog-error"),
            )
            is SongCatalogUiState.Ready -> {
                if (state.catalog.orderedTitles.isEmpty()) {
                    EmptyContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .testTag("catalog-empty"),
                    )
                } else {
                    ReadyContent(
                        catalog = state.catalog,
                        listState = listState,
                        indicatorLetter = indicatorLetter,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        onSongSelected = onSongSelected,
                        onSectionSelected = { section ->
                            val destination = state.catalog.sectionIndex.getPositionForSection(section)
                            if (destination != null) {
                                pendingSectionScroll?.cancel()
                                pendingSectionScroll = scope.launch {
                                    // Jump atomically so list-position observation cannot keep
                                    // restarting the section indicator during a long animation.
                                    listState.scrollToItem(destination)
                                    showIndicator(state.catalog.initials.getOrNull(destination))
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadyContent(
    catalog: SongCatalog,
    listState: LazyListState,
    indicatorLetter: String?,
    modifier: Modifier,
    onSongSelected: (String, String) -> Unit,
    onSectionSelected: (Int) -> Unit,
) {
    Box(modifier = modifier) {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .testTag("song-list"),
                state = listState,
            ) {
                items(
                    items = catalog.orderedTitles,
                    key = { it },
                ) { title ->
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { role = Role.Button }
                            .clickable {
                                onSongSelected(title, catalog.lyricsByTitle[title].orEmpty())
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LetterSidebar(
                sectionIndex = catalog.sectionIndex,
                onSectionSelected = onSectionSelected,
                modifier = Modifier.testTag("letter-sidebar"),
            )
        }
        if (indicatorLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("letter-indicator")
                    .semantics(mergeDescendants = true) {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = indicatorLetter,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.catalog_loading), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(reason: String, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.catalog_error), style = MaterialTheme.typography.titleMedium)
        Text(reason, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.catalog_retry))
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.catalog_empty), style = MaterialTheme.typography.bodyLarge)
    }
}
