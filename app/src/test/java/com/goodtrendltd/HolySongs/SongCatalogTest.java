package com.goodtrendltd.HolySongs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.goodtrendltd.HolySongs.helpers.XMLParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SongCatalogTest {
    private static final String REPOSITORY_ROOT_PROPERTY = "repositoryRoot";

    private Path repositoryRoot() {
        String root = System.getProperty(REPOSITORY_ROOT_PROPERTY);
        if (root == null || root.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required system property '" + REPOSITORY_ROOT_PROPERTY + "'.");
        }
        return Paths.get(root).toAbsolutePath().normalize();
    }

    @Test
    public void preservesOriginalCatalogShape() throws Exception {
        Path root = repositoryRoot();
        String xml = new String(Files.readAllBytes(root.resolve("assets/songs.xml")), StandardCharsets.UTF_8);

        XMLParser parser = new XMLParser();
        Document doc = parser.getDomElement(xml);
        assertNotNull("XMLParser should parse the bundled catalog", doc);

        NodeList songs = doc.getElementsByTagName("song");
        assertEquals("Preserve original order and all 422 entries", 422, songs.getLength());

        List<String> sourceTitles = new ArrayList<String>();
        List<String> duplicateTitleNames = new ArrayList<String>();
        Set<String> distinctTitles = new HashSet<String>();

        for (int i = 0; i < songs.getLength(); i++) {
            Element song = (Element) songs.item(i);
            String title = parser.getValue(song, "name");
            sourceTitles.add(title);

            if (!distinctTitles.add(title)) {
                if (duplicateTitleNames.isEmpty() || !duplicateTitleNames.get(duplicateTitleNames.size() - 1).equals(title)) {
                    duplicateTitleNames.add(title);
                }
            }
        }

        assertEquals("414 distinct source titles", 414, distinctTitles.size());
        assertEquals(
                Arrays.asList(
                        "耶稣耶稣",
                        "耶稣基督是主",
                        "全地宣告",
                        "像天空的鸽子",
                        "轻轻听",
                        "愿您崇高",
                        "天堂在我心",
                        "以色列的圣者"),
                duplicateTitleNames);

        assertEquals("云上太阳", sourceTitles.get(0));
        assertEquals("我一生", sourceTitles.get(sourceTitles.size() - 1));

        String firstLyric = parser.getValue((Element) songs.item(0), "lyric");
        assertTrue(firstLyric.contains("云上太阳"));
        assertTrue(firstLyric.contains("阿哈……它不改变"));
        assertTrue(firstLyric.contains("\n"));

        String duplicateWinnerLyric = parser.getValue((Element) songs.item(320), "lyric");
        assertTrue(duplicateWinnerLyric.contains("轻轻细心听　轻轻细心听您声"));
        assertTrue(duplicateWinnerLyric.contains("\n"));
    }
}
