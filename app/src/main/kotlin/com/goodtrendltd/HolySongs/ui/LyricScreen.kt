package com.goodtrendltd.HolySongs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goodtrendltd.HolySongs.R
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import androidx.compose.material3.ExperimentalMaterial3Api

/** Compose lyric reader. Navigation and sharing remain Activity-owned callbacks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricScreen(
    title: String,
    lyric: String,
    preferences: ReaderPreferenceSnapshot,
    onShare: () -> Unit,
    onVideo: (String) -> Unit,
) {
    var providerMenu by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberSaveable(title, saver = LazyListState.Saver) { LazyListState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    TextButton(onClick = onShare) { Text("分享歌词") }
                    TextButton(onClick = { providerMenu = !providerMenu }) { Text("视频") }
                },
            )
        },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 16.dp),
        ) {
            if (providerMenu) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ProviderButton(R.string.youtube_menu, "youtube") { providerMenu = false; onVideo(it) }
                    ProviderButton(R.string.douyin_menu, "douyin") { providerMenu = false; onVideo(it) }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
            ) {
                item(key = "title") {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color(0xFF00BFFF),
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                item(key = "lyric") {
                    Text(
                        text = lyric,
                        fontSize = preferences.effectiveFontSize.sp,
                        lineHeight = (preferences.effectiveFontSize + 20).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderButton(label: Int, provider: String, onSelected: (String) -> Unit) {
    Button(onClick = { onSelected(provider) }) {
        androidx.compose.material3.Text(androidx.compose.ui.res.stringResource(label))
    }
}
