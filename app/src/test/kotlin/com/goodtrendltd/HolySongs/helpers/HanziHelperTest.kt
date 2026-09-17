package com.goodtrendltd.HolySongs.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

class HanziHelperTest {
    @Test
    fun convertsChineseCharactersToFirstPinyinReadingWithoutTone() {
        assertEquals("ni", HanziHelper.char2Pinyin('你'))
        assertEquals("hao", HanziHelper.char2Pinyin('好'))
        assertEquals("lüe", HanziHelper.words2Pinyin("略"))
        assertEquals("nihaozhuyesu", HanziHelper.words2Pinyin("你好主耶稣"))
    }

    @Test
    fun preservesFirstReadingForPolyphonicCharactersRatherThanContextualPronunciation() {
        assertEquals("zhong", HanziHelper.char2Pinyin('重'))
        assertEquals("xing", HanziHelper.char2Pinyin('行'))
        assertEquals("zhang", HanziHelper.char2Pinyin('长'))
        assertEquals("le", HanziHelper.char2Pinyin('乐'))
        assertEquals("zhongqingyinxingyinlezhangda", HanziHelper.words2Pinyin("重庆银行音乐长大"))
    }

    @Test
    fun usesLowercaseTonelessUnicodeUmlautRatherThanVOrAsciiColon() {
        assertEquals("nü", HanziHelper.char2Pinyin('女'))
        assertEquals("lüe", HanziHelper.char2Pinyin('略'))
        assertEquals("nülüe", HanziHelper.words2Pinyin("女略"))
    }

    @Test
    fun leavesNonChineseCharactersAsTheirOriginalText() {
        assertEquals("A", HanziHelper.char2Pinyin('A'))
        assertEquals("1", HanziHelper.char2Pinyin('1'))
        assertEquals("A1zhu", HanziHelper.words2Pinyin("A1主"))
        assertEquals(" ", HanziHelper.char2Pinyin(' '))
        assertEquals("", HanziHelper.words2Pinyin(""))
        assertEquals("\n", HanziHelper.char2Pinyin('\n'))
        assertEquals("　", HanziHelper.char2Pinyin('\u3000'))
        assertEquals("é!?", HanziHelper.words2Pinyin("é!?"))
        assertEquals("😀Azhu\n　!", HanziHelper.words2Pinyin("😀A主\n　!"))
    }
}
