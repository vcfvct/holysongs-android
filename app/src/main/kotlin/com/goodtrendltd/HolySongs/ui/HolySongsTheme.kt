package com.goodtrendltd.HolySongs.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Applies the stored reader theme without consulting system or dynamic color settings. */
@Composable
fun HolySongsTheme(
    nightMode: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (nightMode) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
