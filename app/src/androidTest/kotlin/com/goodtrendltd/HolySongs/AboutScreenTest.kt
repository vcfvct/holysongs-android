package com.goodtrendltd.HolySongs

import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.ActivityNotFoundException
import com.goodtrendltd.HolySongs.ui.ABOUT_CONTENT
import com.goodtrendltd.HolySongs.ui.AboutLinkActivation
import com.goodtrendltd.HolySongs.ui.SafeAboutLinkSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import android.text.Spanned
import android.text.style.URLSpan
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @Test
    fun aboutContentKeepsTheLegacyTextAndBothExplicitLinkDestinations() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<AboutActivity>(Intent(context, AboutActivity::class.java)).use { scenario ->
            scenario.onActivity { activity ->
                val textView = findTextView(activity.window.decorView)
                assertEquals(ABOUT_CONTENT, textView.text.toString())
                val spanned = textView.text as Spanned
                val links = spanned.getSpans(0, spanned.length, SafeAboutLinkSpan::class.java)
                assertTrue(links.any { it.destination == "http://www.cccgermantown.org" })
                assertTrue(links.any { it.destination == "mailto:vcfvct@gmail.com" })
            }
        }
    }

    @Test
    fun aboutLinkActivationStaysSafeWhenNoHandlerOrLaunchFails() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        var launched = false
        assertTrue(AboutLinkActivation.activate(context, "https://example.invalid") { launched = true })
        assertTrue(launched)
        assertFalse(AboutLinkActivation.activate(context, "https://example.invalid") {
            throw ActivityNotFoundException("no handler")
        })
        assertFalse(AboutLinkActivation.activate(context, "mailto:vcfvct@gmail.com") {
            throw IllegalStateException("window unavailable")
        })
    }

    private fun findTextView(view: android.view.View): TextView {
        if (view is TextView && view.text.toString() == ABOUT_CONTENT) return view
        if (view is android.view.ViewGroup) {
            for (index in 0 until view.childCount) {
                runCatching { return findTextView(view.getChildAt(index)) }
            }
        }
        error("About text view not found")
    }
}
