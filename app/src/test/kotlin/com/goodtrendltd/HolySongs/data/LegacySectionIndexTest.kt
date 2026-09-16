package com.goodtrendltd.HolySongs.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Characterizes the captured legacy SectionIndexer destinations, not a replacement algorithm. */
class LegacySectionIndexTest {
    private data class CapturedSection(val letter: String, val position: Int, val present: Boolean)

    private fun resourceLines(path: String): List<String> {
        val stream = javaClass.getResourceAsStream(path)
        requireNotNull(stream) { "Missing frozen test resource $path" }
        return stream.use { input ->
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).readLines()
        }
    }

    private fun capturedSections(): List<CapturedSection> = resourceLines("/legacy-api37-sections.tsv")
        .drop(1)
        .filter { it.isNotBlank() }
        .map { line ->
            val fields = line.split('\t')
            require(fields.size == 3) { "Malformed section fixture row: $line" }
            CapturedSection(fields[0], fields[1].toInt(), fields[2].toBooleanStrict())
        }

    private fun capturedOrder(): List<String> = resourceLines("/legacy-api37-order.txt")
        .filter { it.isNotEmpty() }

    @Test
    fun preservesEveryApi37PresentAndAbsentDestinationFromLegacyCapture() {
        val orderedTitles = capturedOrder()
        val captured = capturedSections()
        assertEquals(414, orderedTitles.size)
        assertEquals((('A'..'Z').map { it.toString() }), captured.map { it.letter })

        val index = LegacySectionIndex(orderedTitles)
        assertEquals(('A'..'Z').map { it.toString() }, index.sections)
        captured.forEach { expected ->
            val actual = index.getPositionForSection(expected.letter[0] - 'A')
            assertEquals(
                "API37 legacy destination for ${expected.letter} (present=${expected.present})",
                expected.position,
                actual,
            )
            assertTrue("Captured destination remains within the captured catalog", expected.position in orderedTitles.indices)
            // `present` is retained as fixture metadata; absent letters intentionally keep
            // their captured native destination rather than being converted to null.
        }
    }

    @Test
    fun returnsNullForInvalidSectionsOnEmptyAndCapturedNonEmptyCatalogs() {
        val empty = LegacySectionIndex(emptyList())
        assertEquals(('A'..'Z').map { it.toString() }, empty.sections)
        (-2..27).forEach { section -> assertNull(empty.getPositionForSection(section)) }
        assertNull(empty.getPositionForSection(Int.MIN_VALUE))
        assertNull(empty.getPositionForSection(Int.MAX_VALUE))

        val nonEmpty = LegacySectionIndex(capturedOrder())
        listOf(-1, 26, Int.MIN_VALUE, Int.MAX_VALUE).forEach { section ->
            assertNull("Invalid section $section must not clamp a non-empty catalog", nonEmpty.getPositionForSection(section))
        }
    }

    @Test
    fun handlesEmptyTitlesNumericNonChineseAndSmallOrdersWithoutUnsafeDestinations() {
        val edgeOrders = listOf(
            emptyList<String>(),
            listOf(""),
            listOf("", ""),
            listOf("123"),
            listOf("ABC"),
            listOf("123", "ABC", "非中文"),
            listOf("", "123", "ABC", "非中文"),
        )
        edgeOrders.forEach { orderedTitles ->
            val index = LegacySectionIndex(orderedTitles)
            (0..25).forEach { section ->
                val position = try {
                    index.getPositionForSection(section)
                } catch (failure: Throwable) {
                    fail("Section $section threw for order $orderedTitles: $failure")
                    null
                }
                if (position != null) {
                    assertTrue(
                        "Section $section returned an unsafe $position for order $orderedTitles",
                        position in orderedTitles.indices,
                    )
                }
            }
        }
    }
}
