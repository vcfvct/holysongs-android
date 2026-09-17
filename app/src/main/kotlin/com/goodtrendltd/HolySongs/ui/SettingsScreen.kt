package com.goodtrendltd.HolySongs.ui

import android.widget.Switch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.goodtrendltd.HolySongs.R
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot

private val fontSizeChoices = listOf(16, 18, 20, 22, 24, 26, 28, 30)

/** Compose settings surface backed by the legacy appPrefFile contract. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: ReaderPreferenceSnapshot,
    onFontSizeSelected: (Int) -> Unit,
    onResetFontSize: () -> Unit,
    onNightModeChanged: (Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Scaffold owns the edge-to-edge insets; consume its padding once here.
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .testTag("settings-content"),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "选择字体大小 当前为: ${preferences.effectiveFontSize}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("font-size-current"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { menuExpanded = true }) {
                    Text("字体大小")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    fontSizeChoices.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size.toString()) },
                            onClick = {
                                menuExpanded = false
                                onFontSizeSelected(size)
                            },
                        )
                    }
                }
                Button(onClick = onResetFontSize) {
                    Text(stringResource(R.string.font_size_resume_default))
                }
            }
            // Keep an Android Switch with the historical id for Java/View interoperability while
            // the surrounding screen and state remain Compose-owned.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("夜间模式(黑色背景)")
                AndroidView(
                    modifier = Modifier.testTag("night-mode-switch"),
                    factory = { context ->
                        Switch(context).apply {
                            id = R.id.nightModeSwitch
                            contentDescription = "夜间模式(黑色背景)"
                        }
                    },
                    update = { view ->
                        view.setOnCheckedChangeListener(null)
                        view.isChecked = preferences.nightMode
                        view.setOnCheckedChangeListener { _, checked -> onNightModeChanged(checked) }
                    },
                )
            }
        }
    }
}
