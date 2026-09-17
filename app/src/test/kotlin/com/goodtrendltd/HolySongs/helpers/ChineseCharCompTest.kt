package com.goodtrendltd.HolySongs.helpers

import java.text.Collator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChineseCharCompTest {
    @Test
    fun comparesUsingChinaLocaleCollation() {
        val comparator = ChineseCharComp()
        val collator = Collator.getInstance(Locale.CHINA)

        assertEquals(Integer.signum(collator.compare("啊", "把")), Integer.signum(comparator.compare("啊", "把")))
        assertEquals(Integer.signum(collator.compare("耶稣", "主")), Integer.signum(comparator.compare("耶稣", "主")))
        assertEquals(0, comparator.compare("主", "主"))
    }

    @Test
    fun satisfiesComparatorLawsForChinesePolyphonicAndFallbackTitles() {
        val comparator = ChineseCharComp()
        val collator = Collator.getInstance(Locale.CHINA)
        val titles = arrayOf("", " ", "　", "啊", "把", "测", "主", "主", "耶稣", "轻轻听",
            "重", "行", "乐", "长", "A", "a", "1", "é", "e\u0301", "😀")

        for (a in titles) {
            assertEquals("Reflexivity: $a", 0, comparator.compare(a, a))
            for (b in titles) {
                val ab = comparator.compare(a, b)
                assertEquals(
                    "China Collator delegation: $a/$b",
                    Integer.signum(collator.compare(a, b)),
                    ab,
                )
                assertEquals(
                    "Antisymmetry: $a/$b",
                    -ab,
                    comparator.compare(b, a),
                )
                for (c in titles) {
                    val bc = comparator.compare(b, c)
                    val ac = comparator.compare(a, c)
                    if (ab <= 0 && bc <= 0) {
                        assertTrue("Transitivity: $a/$b/$c", ac <= 0)
                    }
                    if (ab == 0) {
                        assertEquals("Equivalent titles compare consistently against $c", bc, ac)
                    }
                }
            }
        }
    }
}
