package com.goodtrendltd.HolySongs.data

import com.goodtrendltd.HolySongs.helpers.HanziHelper
import java.util.Collections

/**
 * The legacy SongTitleAdapter SectionIndexer search, scoped to one immutable title order.
 *
 * The sparse cache is intentionally retained: a section is searched once and subsequent
 * requests return the same destination, as they did in the adapter.
 */
class LegacySectionIndex(orderedTitles: List<String>) {
    private val items: List<String> =
        Collections.unmodifiableList(ArrayList(orderedTitles))
    private val alphaMap = HashMap<Int, Int>()

    val sections: List<String> =
        Collections.unmodifiableList(('A'..'Z').map { it.toString() })

    fun getPositionForSection(section: Int): Int? {
        if (section !in sections.indices || items.isEmpty()) return null
        if (section == 0) {
            return if (initialAt(0) != null) 0 else null
        }

        alphaMap[section]?.let { return it }
        val targetLetter = sections[section]
        var start = approximateStart(section - 1)
        var end = approximateEnd(section)
        if (start < 0 || end < 0 || start >= items.size || end >= items.size) return null

        var position = -1
        while (start < end) {
            position = (start + end) / 2
            val currentLetter = initialAt(position) ?: return null
            val result = currentLetter.compareTo(targetLetter, ignoreCase = true)
            if (result < 0) {
                start = position + 1
            } else if (result > 0) {
                end = position - 1
            } else {
                while (true) {
                    val matchingLetter = initialAt(position) ?: return null
                    if (!matchingLetter.equals(targetLetter, ignoreCase = true)) break
                    if (position >= 1) {
                        position--
                    } else {
                        alphaMap[section] = 0
                        return 0
                    }
                }
                break
            }
        }
        if (position < 0) return null
        val destination = position + 1
        if (destination !in items.indices) return null
        alphaMap[section] = destination
        return destination
    }

    private fun initialAt(position: Int): String? {
        if (position !in items.indices) return null
        return HanziHelper.words2Pinyin(items[position]).firstOrNull()?.toString()
    }

    private fun approximateStart(section: Int): Int {
        var currentSection = section
        var result = -1
        while (currentSection >= 0) {
            result = firstAppearance(currentSection)
            if (result != -1) {
                val target = sections[currentSection]
                while (initialAt(result) == target) {
                    if (result < items.lastIndex) {
                        result++
                    } else {
                        return result
                    }
                }
                return result - 1
            }
            currentSection--
        }
        return result
    }

    private fun approximateEnd(section: Int): Int {
        var currentSection = section
        var result = -1
        while (currentSection < sections.size) {
            result = firstAppearance(currentSection)
            if (result != -1) {
                val target = sections[currentSection]
                while (initialAt(result) == target) {
                    if (result >= 1) {
                        result--
                    } else {
                        return result
                    }
                }
                return result + 1
            }
            currentSection++
        }
        return result
    }

    private fun firstAppearance(section: Int): Int {
        if (section !in sections.indices || items.isEmpty()) return -1
        val targetLetter = sections[section]
        var start = 0
        var end = items.size
        var appearance = -1
        while (start < end) {
            val position = (start + end) / 2
            val currentLetter = initialAt(position) ?: return -1
            val result = currentLetter.compareTo(targetLetter, ignoreCase = true)
            if (result < 0) {
                start = position + 1
            } else if (result > 0) {
                end = position - 1
            } else {
                appearance = position
                break
            }
        }
        return appearance
    }
}
