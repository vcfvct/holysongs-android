package com.goodtrendltd.HolySongs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Planned property name: repositoryRoot.
 * T011 will set this portable property to the repository root before Gradle/JUnit runs.
 * This test intentionally fails clearly if the property is missing so that the build does not
 * silently read the wrong directory.
 */
public class BuildInputsTest {
    private static final String SONGS_SHA_256 = "88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89";
    private static final String PINYIN_JAR_SHA_256 = "6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68";
    private static final String REPOSITORY_ROOT_PROPERTY = "repositoryRoot";

    private static Path repositoryRoot() {
        String root = System.getProperty(REPOSITORY_ROOT_PROPERTY);
        if (root == null || root.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required system property '" + REPOSITORY_ROOT_PROPERTY
                            + "'. T011 will provide the repository root before JUnit execution.");
        }
        return Paths.get(root).toAbsolutePath().normalize();
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(path);
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte value : hash) {
            sb.append(String.format("%02x", value));
        }
        return sb.toString();
    }

    @Test
    public void sourceAssetAndJarStayOnBaselineHashes() throws Exception {
        Path root = repositoryRoot();

        assertEquals("assets/songs.xml hash drifted from the recorded baseline",
                SONGS_SHA_256,
                sha256(root.resolve("assets/songs.xml")));
        assertEquals("libs/pinyin4j-2.5.0.jar hash drifted from the recorded baseline",
                PINYIN_JAR_SHA_256,
                sha256(root.resolve("libs/pinyin4j-2.5.0.jar")));
    }

    @Test
    public void preferencesUseLegacyResourceIdentifiers() throws Exception {
        Path root = repositoryRoot();
        Path stringsXml = root.resolve("res/values/strings.xml");
        assertTrue("Missing preference resource file: " + stringsXml, Files.exists(stringsXml));

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringsXml.toFile());
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getElementsByTagName("string");
        Map<String, String> values = new HashMap<String, String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String name = element.getAttribute("name");
                values.put(name, element.getTextContent());
            }
        }

        assertEquals("appPrefFile", values.get("app_pref"));
        assertEquals("fontSize", values.get("font_size_pref_key"));
        assertEquals("nightMode", values.get("night_mode_pref_key"));
    }
}
