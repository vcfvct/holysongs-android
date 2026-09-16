package com.goodtrendltd.HolySongs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.goodtrendltd.HolySongs.helpers.XMLParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Raw-source characterization, not an Android sorted-order or rendered-lyric fixture. */
public class SongCatalogTest {
    private final XMLParser parser = new XMLParser();

    private Path repositoryRoot() {
        String root = System.getProperty("repositoryRoot");
        if (root == null || root.trim().isEmpty()) {
            throw new IllegalStateException("Missing required system property 'repositoryRoot'.");
        }
        return Paths.get(root).toAbsolutePath().normalize();
    }

    private NodeList actualSongs() throws Exception {
        String xml = new String(Files.readAllBytes(repositoryRoot().resolve("assets/songs.xml")),
                StandardCharsets.UTF_8);
        Document doc = parser.getDomElement(xml);
        assertNotNull("The real XMLParser must parse the bundled source", doc);
        return doc.getElementsByTagName("song");
    }

    private NodeList baselineSongs() throws Exception {
        // Independent, committed input snapshot: never generated from XMLParser or the working
        // asset during tests. Expectations use JAXP, not the production extraction helper.
        try (InputStream fixture = getClass().getResourceAsStream("/catalog-baseline.xml")) {
            assertNotNull("Missing frozen catalog-baseline.xml test resource", fixture);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fixture);
            return doc.getElementsByTagName("song");
        }
    }

    @Test
    public void preservesEveryRawSourceFieldAtItsOriginalPosition() throws Exception {
        NodeList actual = actualSongs();
        NodeList expected = baselineSongs();
        assertEquals("Frozen input contains all 422 source entries", 422, expected.getLength());
        assertEquals("All 422 entries stay in original order", 422, actual.getLength());

        for (int position = 0; position < expected.getLength(); position++) {
            Element source = (Element) actual.item(position);
            Element baseline = (Element) expected.item(position);
            assertEquals("Raw parsed name at zero-based XML position " + position,
                    baseline.getElementsByTagName("name").item(0).getTextContent(),
                    parser.getValue(source, "name"));
            assertEquals("Raw lyric including line breaks and original text at position " + position,
                    baseline.getElementsByTagName("lyric").item(0).getTextContent(),
                    parser.getValue(source, "lyric"));
        }
        assertEquals("云上太阳", parser.getValue((Element) actual.item(0), "name"));
        assertEquals("我一生", parser.getValue((Element) actual.item(421), "name"));
    }

    @Test
    public void preservesBothPositionsAndBothLyricsOfEveryDuplicatePair() throws Exception {
        NodeList actual = actualSongs();
        NodeList baseline = baselineSongs();
        Map<String, List<Integer>> positionsByTitle = new LinkedHashMap<String, List<Integer>>();
        for (int position = 0; position < actual.getLength(); position++) {
            String title = parser.getValue((Element) actual.item(position), "name");
            if (!positionsByTitle.containsKey(title)) {
                positionsByTitle.put(title, new ArrayList<Integer>());
            }
            positionsByTitle.get(title).add(position);
        }
        assertEquals("414 distinct raw titles", 414, positionsByTitle.size());

        Map<String, List<Integer>> duplicates = new LinkedHashMap<String, List<Integer>>();
        for (Map.Entry<String, List<Integer>> entry : positionsByTitle.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }
        // Literal, independently reviewed source positions, not inferred from the actual parser.
        Map<String, List<Integer>> expectedPairs = new LinkedHashMap<String, List<Integer>>();
        expectedPairs.put("轻轻听", Arrays.asList(42, 320));
        expectedPairs.put("以色列的圣者", Arrays.asList(53, 373));
        expectedPairs.put("耶稣基督是主", Arrays.asList(87, 123));
        expectedPairs.put("耶稣耶稣", Arrays.asList(102, 110));
        expectedPairs.put("天堂在我心", Arrays.asList(109, 351));
        expectedPairs.put("全地宣告", Arrays.asList(143, 144));
        expectedPairs.put("像天空的鸽子", Arrays.asList(213, 214));
        expectedPairs.put("愿您崇高", Arrays.asList(341, 342));
        assertEquals(expectedPairs, duplicates);

        for (Map.Entry<String, List<Integer>> pair : expectedPairs.entrySet()) {
            for (int position : pair.getValue()) {
                Element expected = (Element) baseline.item(position);
                assertEquals(pair.getKey() + " raw lyric at " + position,
                        expected.getElementsByTagName("lyric").item(0).getTextContent(),
                        parser.getValue((Element) actual.item(position), "lyric"));
            }
        }
    }

    @Test
    public void preservesExactRepresentativeRawWhitespaceAndChineseLyrics() throws Exception {
        NodeList actual = actualSongs();
        assertEquals("\n"
                        + "            无论是住在美丽的高山，或是躺卧在阴暗的幽谷，\n"
                        + "            当你抬起头，你将会发现，主已为你我而预备。\n"
                        + "            云上太阳它总不改变，虽然小雨洒在脸上，\n"
                        + "            云上太阳它总不改变，阿哈……它不改变。\n"
                        + "        ",
                parser.getValue((Element) actual.item(0), "lyric"));
        assertEquals("\n"
                        + "            轻轻细心听\u3000轻轻细心听您声\n"
                        + "            轻轻细心听\u3000轻轻细心听您声\n"
                        + "            细细听\u3000轻轻细细听\n"
                        + "            倾听您说话儿共对应\n"
                        + "            细细说\u3000轻轻细细说\n"
                        + "            因知道我牧人在细听\n"
                        + "            上帝您是我独一生命光\n"
                        + "            羊属您必清楚听您声\n"
                        + "            牧养引导我\u3000轻声教导我\n"
                        + "            一生听您话儿共对应\n"
                        + "        ",
                parser.getValue((Element) actual.item(320), "lyric"));
    }

    @Test
    public void characterizesFirstTextNodeExtractionWithoutTrimmingOrJoining() {
        Document doc = parser.getDomElement("<song><name> A　主 </name>"
                + "<lyric> first\n<verse>nested</verse>last </lyric></song>");
        assertNotNull(doc);
        Element song = doc.getDocumentElement();
        assertEquals(" A　主 ", parser.getValue(song, "name"));
        assertEquals(" first\n", parser.getValue(song, "lyric"));
        assertEquals("", parser.getValue(song, "missing"));
    }
}
