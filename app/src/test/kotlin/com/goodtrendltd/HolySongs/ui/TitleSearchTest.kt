package com.goodtrendltd.HolySongs.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class TitleSearchTest {
    private val titles = listOf(
        "Alpha 12",
        "敬拜！赞美",
        "Song-BETA",
        "𠀀之歌",
    )

    @Test
    fun emptyAndWhitespaceOnlyQueriesReturnTheUnchangedOrderedInput() {
        assertSame(titles, filterTitlesByQuery(titles, ""))
        assertSame(titles, filterTitlesByQuery(titles, " \t\n"))
    }

    @Test
    fun trimsOnlyQueryBoundariesAndMatchesLiteralUnicodeSubstrings() {
        assertEquals(listOf("敬拜！赞美"), filterTitlesByQuery(titles, "  拜！赞  "))
        assertEquals(listOf("Alpha 12"), filterTitlesByQuery(titles, "ha 1"))
        assertEquals(listOf("𠀀之歌"), filterTitlesByQuery(titles, "𠀀之"))
        assertEquals(emptyList<String>(), filterTitlesByQuery(titles, "拜赞"))
    }

    @Test
    fun comparesLatinLettersWithoutCaseAndPreservesCatalogOrder() {
        assertEquals(
            listOf("Alpha 12", "Song-BETA"),
            filterTitlesByQuery(titles, "A"),
        )
        assertEquals(listOf("Song-BETA"), filterTitlesByQuery(titles, "beta"))
    }

    @Test
    fun longAndAbsentQueriesReturnANewEmptyResultWithoutMutatingInput() {
        val original = titles.toList()
        val result = filterTitlesByQuery(titles, "不存在".repeat(1_000))

        assertEquals(emptyList<String>(), result)
        assertNotSame(titles, result)
        assertEquals(original, titles)
    }
}
