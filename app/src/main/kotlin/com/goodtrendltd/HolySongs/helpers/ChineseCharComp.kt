package com.goodtrendltd.HolySongs.helpers

import java.text.Collator
import java.util.Locale

class ChineseCharComp : Comparator<String> {
    override fun compare(lhs: String?, rhs: String?): Int {
        val left = lhs ?: ""
        val right = rhs ?: ""
        val collator = Collator.getInstance(Locale.CHINA)
        val result = collator.compare(left, right)
        return when {
            result < 0 -> -1
            result > 0 -> 1
            else -> 0
        }
    }
}
