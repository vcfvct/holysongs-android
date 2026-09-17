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
    public void preservesFirstReadingForPolyphonicCharactersRatherThanContextualPronunciation() {
        // Independent expectations from the pinned JAR's pinyindb/unicode_to_hanyu_pinyin.txt:
        // 91CD=(zhong4,chong2), 884C=(xing2,hang2,hang4,xing4,heng2),
        // 957F=(zhang3,chang2), 4E50=(le4,yue4). No production-helper-generated expectations.
        assertEquals("zhong", HanziHelper.char2Pinyin('重'));
        assertEquals("xing", HanziHelper.char2Pinyin('行'));
        assertEquals("zhang", HanziHelper.char2Pinyin('长'));
        assertEquals("le", HanziHelper.char2Pinyin('乐'));
        assertEquals("zhongqingyinxingyinlezhangda", HanziHelper.words2Pinyin("重庆银行音乐长大"));
    }

    @Test
    public void usesLowercaseTonelessUnicodeUmlautRatherThanVOrAsciiColon() {
        assertEquals("nü", HanziHelper.char2Pinyin('女'));
        assertEquals("lüe", HanziHelper.char2Pinyin('略'));
        assertEquals("nülüe", HanziHelper.words2Pinyin("女略"));
    }

    @Test
    public void leavesNonChineseCharactersAsTheirOriginalText() {
        assertEquals("A", HanziHelper.char2Pinyin('A'));
        assertEquals("1", HanziHelper.char2Pinyin('1'));
        assertEquals("A1zhu", HanziHelper.words2Pinyin("A1主"));
        assertEquals(" ", HanziHelper.char2Pinyin(' '));
        assertEquals("", HanziHelper.words2Pinyin(""));
        assertEquals("\n", HanziHelper.char2Pinyin('\n'));
        assertEquals("\u3000", HanziHelper.char2Pinyin('\u3000'));
        assertEquals("é!?", HanziHelper.words2Pinyin("é!?"));
        assertEquals("😀Azhu\n　!", HanziHelper.words2Pinyin("😀A主\n　!"));
    }
}
