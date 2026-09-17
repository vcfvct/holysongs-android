package com.goodtrendltd.HolySongs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Measured A-Z rail used by the song list.  The rail owns pointer coordination so a compact
 * drag cannot also become a list scroll or a release selection.
 */
@Composable
fun LetterSidebar(
    sectionIndex: com.goodtrendltd.HolySongs.data.LegacySectionIndex,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = remember { ('A'..'Z').map(Char::toString) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    val measuredTextHeight = remember(textMeasurer, textStyle, density.fontScale) {
        textMeasurer.measure(AnnotatedString("A"), style = textStyle).size.height
    }
    val rowHeightPx = remember(measuredTextHeight, density.density, density.fontScale) {
        measuredTextHeight + with(density) { 8.dp.roundToPx() }
    }.coerceAtLeast(1)
    val rowHeight = with(density) { rowHeightPx.toDp() }
    var railWidthPx by remember { mutableIntStateOf(0) }
    var railHeightPx by remember { mutableIntStateOf(0) }
    val railScroll = rememberScrollState()
    var committedLetter by remember { mutableStateOf<String?>(null) }
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val lifecycleOwner = LocalLifecycleOwner.current
    var pointerEpoch by remember { mutableIntStateOf(0) }
    val fitMode = railHeightPx > 0 && rowHeightPx.toLong() * letters.size <= railHeightPx.toLong()
    val compactMode = !fitMode
    val latestOnSectionSelected by androidx.compose.runtime.rememberUpdatedState(onSectionSelected)

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP ||
                event == androidx.lifecycle.Lifecycle.Event.ON_START
            ) {
                // Restart the arbiter after lifecycle transitions so an old pointer cannot use
                // coordinates from a detached or backgrounded rail.
                committedLetter = null
                pointerEpoch++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun commit(letter: String) {
        val section = letter[0] - 'A'
        if (sectionIndex.getPositionForSection(section) != null) {
            committedLetter = letter
            latestOnSectionSelected(section)
        }
    }

    fun letterAt(y: Float): String {
        val contentY = (y + if (compactMode) railScroll.value.toFloat() else 0f)
            .coerceIn(0f, (letters.size * rowHeightPx - 1).toFloat())
        return letters[(contentY / rowHeightPx).toInt().coerceIn(0, letters.lastIndex)]
    }

    val pointerModifier = Modifier.pointerInput(
        compactMode,
        rowHeightPx,
        touchSlop,
        railWidthPx,
        railHeightPx,
        pointerEpoch,
    ) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            val primaryPointerId = down.id
            val start = down.position
            var previous = start
            var dragged = false
            var cancelled = false
            var lastGestureLetter: String? = null
            fun commitAt(y: Float) {
                val letter = letterAt(y)
                if (lastGestureLetter != letter) {
                    lastGestureLetter = letter
                    commit(letter)
                }
            }
            // De-duplicate only within this gesture. A later tap on the same letter must still
            // navigate back after the user manually scrolls the song list elsewhere.
            if (!compactMode) commitAt(start.y)
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == primaryPointerId } ?: break
                if (event.type == PointerEventType.Release) {
                    if (!cancelled && (!compactMode || !dragged)) commitAt(change.position.y)
                    change.consume()
                    break
                }
                if (!change.pressed) {
                    cancelled = true
                    break
                }
                val current = change.position
                val totalDistance = (current - start).getDistance()
                if (totalDistance > touchSlop) dragged = true
                if (!compactMode) {
                    // Fit mode selects immediately and follows every row crossed by a held
                    // pointer; release only repeats the final row, which commit de-duplicates.
                    commitAt(current.y)
                    change.consume()
                } else if (dragged) {
                    val delta = current.y - previous.y
                    if (delta != 0f) {
                        // A finger moving upward exposes lower letters.  The list never sees this
                        // consumed pointer because the rail owns the gesture from slop onward.
                        railScroll.dispatchRawDelta(-delta)
                    }
                    change.consume()
                }
                previous = current
            }
        }
    }

    Box(
        modifier = modifier
            .width(56.dp)
            .fillMaxHeight()
            .clipToBounds()
            .onSizeChanged {
                if (railWidthPx != it.width || railHeightPx != it.height) committedLetter = null
                railWidthPx = it.width
                railHeightPx = it.height
            }
            .then(
                if (compactMode) {
                    Modifier.semantics {
                        verticalScrollAxisRange = ScrollAxisRange(
                            value = { railScroll.value.toFloat() },
                            maxValue = { railScroll.maxValue.toFloat() },
                            reverseScrolling = false,
                        )
                        scrollBy { _, y ->
                            val before = railScroll.value
                            railScroll.dispatchRawDelta(y)
                            railScroll.value != before
                        }
                    }
                } else {
                    Modifier.semantics {
                        verticalScrollAxisRange = ScrollAxisRange(
                            value = { 0f }, maxValue = { 0f }, reverseScrolling = false
                        )
                    }
                }
            )
            .then(pointerModifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Measure all rows against an unbounded scroll child while the Box supplies the
                // finite viewport. The parent pointer arbiter remains the only touch scroller.
                .verticalScroll(railScroll, enabled = false),
        ) {
            letters.forEach { letter ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .testTag("letter-$letter")
                        .semantics(mergeDescendants = true) {
                            contentDescription = letter
                            role = Role.Button
                            selected = committedLetter == letter
                            onClick {
                                commit(letter)
                                true
                            }
                            // Each row remains discoverable as a standard action while the parent
                            // pointer handler owns drag arbitration.
                        }
                        .then(Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(text = letter, style = textStyle)
                }
            }
        }
    }
}
