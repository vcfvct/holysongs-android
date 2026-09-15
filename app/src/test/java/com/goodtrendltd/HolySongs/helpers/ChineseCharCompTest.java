package com.goodtrendltd.HolySongs.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.Locale;
import org.junit.Test;

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
    public void remainsTransitiveForChineseStrings() {
        ChineseCharComp comparator = new ChineseCharComp();

        assertTrue(comparator.compare("啊", "把") < 0);
        assertTrue(comparator.compare("把", "测") < 0);
        assertTrue(comparator.compare("啊", "测") < 0);
    }
}
