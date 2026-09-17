package com.goodtrendltd.HolySongs.helpers

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

object HanziHelper {
    private val format = HanyuPinyinOutputFormat().apply {
        toneType = HanyuPinyinToneType.WITHOUT_TONE
        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
        caseType = HanyuPinyinCaseType.LOWERCASE
    }

    @JvmStatic
    fun char2Pinyin(c: Char): String {
        val pinyin = try {
            PinyinHelper.toHanyuPinyinStringArray(c, format)
        } catch (_: BadHanyuPinyinOutputFormatCombination) {
            null
        }
        return if (pinyin == null) {
            c.toString()
        } else {
            pinyin[0]
        }
    }

    @JvmStatic
    fun words2Pinyin(words: String): String {
        val builder = StringBuilder()
        for (char in words.toCharArray()) {
            builder.append(char2Pinyin(char))
        }
        return builder.toString()
    }
}
