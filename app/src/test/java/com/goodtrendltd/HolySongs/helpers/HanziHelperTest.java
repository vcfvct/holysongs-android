package com.goodtrendltd.HolySongs.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HanziHelperTest {
    @Test
    public void convertsChineseCharactersToFirstPinyinReadingWithoutTone() {
        assertEquals("ni", HanziHelper.char2Pinyin('你'));
        assertEquals("hao", HanziHelper.char2Pinyin('好'));
        assertEquals("lüe", HanziHelper.words2Pinyin("略"));
        assertEquals("nihaozhuyesu", HanziHelper.words2Pinyin("你好主耶稣"));
    }

    @Test
    public void leavesNonChineseCharactersAsTheirOriginalText() {
        assertEquals("A", HanziHelper.char2Pinyin('A'));
        assertEquals("1", HanziHelper.char2Pinyin('1'));
        assertEquals("A1zhu", HanziHelper.words2Pinyin("A1主"));
        assertEquals(" ", HanziHelper.char2Pinyin(' '));
    }
}
