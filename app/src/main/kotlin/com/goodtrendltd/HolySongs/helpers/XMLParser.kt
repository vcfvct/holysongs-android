package com.goodtrendltd.HolySongs.helpers

import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class XMLParser {
    fun getDomElement(xml: String?): Document? {
        if (xml == null) return null
        return try {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val source = InputSource().apply { characterStream = StringReader(xml) }
            builder.parse(source)
        } catch (e: Exception) {
            Log.e("Error: ", e.message ?: "")
            null
        }
    }

    fun getValue(item: Element?, str: String): String {
        val elements = item?.getElementsByTagName(str) ?: return ""
        return getElementValue(elements.item(0))
    }

    fun getElementValue(elem: Node?): String {
        if (elem != null && elem.hasChildNodes()) {
            var child = elem.firstChild
            while (child != null) {
                if (child.nodeType == Node.TEXT_NODE) {
                    return child.nodeValue
                }
                child = child.nextSibling
            }
        }
        return ""
    }
}
