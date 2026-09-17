package com.goodtrendltd.HolySongs

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.Node

class BuildInputsTest {
    @Test
    fun sourceAssetStaysOnBaselineHash() {
        val root = repositoryRoot()

        assertEquals(
            "app/src/main/assets/songs.xml hash drifted from the recorded baseline",
            SONGS_SHA_256,
            sha256(root.resolve("app/src/main/assets/songs.xml")),
        )
    }

    @Test
    fun preferencesUseLegacyResourceIdentifiers() {
        val root = repositoryRoot()
        val stringsXml = root.resolve("app/src/main/res/values/strings.xml")
        assertTrue("Missing preference resource file: $stringsXml", Files.exists(stringsXml))

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringsXml.toFile())
        document.documentElement.normalize()

        val values = mutableMapOf<String, String>()
        val nodes = document.getElementsByTagName("string")
        for (index in 0 until nodes.length) {
            val node = nodes.item(index)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                values[element.getAttribute("name")] = element.textContent
            }
        }

        assertEquals("appPrefFile", values["app_pref"])
        assertEquals("fontSize", values["font_size_pref_key"])
        assertEquals("nightMode", values["night_mode_pref_key"])
    }

    private fun repositoryRoot(): Path {
        val root = System.getProperty(REPOSITORY_ROOT_PROPERTY)
            ?: throw IllegalStateException(
                "Missing required system property '$REPOSITORY_ROOT_PROPERTY'. app/build.gradle must provide it before JUnit execution.",
            )
        return Paths.get(root).toAbsolutePath().normalize()
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(Files.readAllBytes(path))
        return hash.joinToString(separator = "") { "%02x".format(it) }
    }

    companion object {
        private const val SONGS_SHA_256 = "88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89"
        private const val REPOSITORY_ROOT_PROPERTY = "repositoryRoot"
    }
}
