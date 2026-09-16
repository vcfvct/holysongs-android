package com.goodtrendltd.HolySongs.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.Locale;
import org.junit.Test;

/** Host Collator properties are not API23/API37 ordering fixtures. */
public class ChineseCharCompTest {
    @Test
    public void comparesUsingChinaLocaleCollation() {
        ChineseCharComp comparator = new ChineseCharComp();
        Collator collator = Collator.getInstance(Locale.CHINA);

        assertEquals(Integer.signum(collator.compare("啊", "把")), Integer.signum(comparator.compare("啊", "把")));
        assertEquals(Integer.signum(collator.compare("耶稣", "主")), Integer.signum(comparator.compare("耶稣", "主")));
        assertEquals(0, comparator.compare("主", "主"));
    }

    @Test
    public void satisfiesComparatorLawsForChinesePolyphonicAndFallbackTitles() {
        ChineseCharComp comparator = new ChineseCharComp();
        Collator collator = Collator.getInstance(Locale.CHINA);
        String[] titles = {"", " ", "　", "啊", "把", "测", "主", "主", "耶稣", "轻轻听",
                "重", "行", "乐", "长", "A", "a", "1", "é", "e\u0301", "😀"};

        for (String a : titles) {
            assertEquals("Reflexivity: " + a, 0, comparator.compare(a, a));
            for (String b : titles) {
                int ab = comparator.compare(a, b);
                assertEquals("China Collator delegation: " + a + "/" + b,
                        Integer.signum(collator.compare(a, b)), ab);
                assertEquals("Antisymmetry: " + a + "/" + b,
                        -ab, comparator.compare(b, a));
                for (String c : titles) {
                    int bc = comparator.compare(b, c);
                    int ac = comparator.compare(a, c);
                    if (ab <= 0 && bc <= 0) {
                        assertTrue("Transitivity: " + a + "/" + b + "/" + c, ac <= 0);
                    }
                    if (ab == 0) {
                        assertEquals("Equivalent titles compare consistently against " + c, bc, ac);
                    }
                }
            }
        }
    }
}
