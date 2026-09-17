package com.goodtrendltd.HolySongs.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

/** About content remains byte-for-byte equivalent to the legacy screen, including links. */
const val ABOUT_CONTENT: String = "\n我们是位于马里兰州Germantown的德国镇基督教会，欢迎大家光临。http://www.cccgermantown.org/ " +
    "\n\n 本app为方便团契或其他聚会时大家敬拜之用，至少可以省去打印的麻烦:-) " +
    "\n\n任何意见，请反馈至: vcfvct@gmail.com \n感谢Katie的鼓励和添加歌曲^_^。"

/** Explicitly launches an About destination while keeping the caller screen on failure. */
object AboutLinkActivation {
    fun activate(
        context: Context,
        destination: String,
        launch: (Intent) -> Unit = { context.startActivity(it) },
    ): Boolean {
        return try {
            launch(Intent(Intent.ACTION_VIEW, Uri.parse(destination)))
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: RuntimeException) {
            false
        }
    }
}

/** A URLSpan with an explicit, testable launch boundary rather than implicit auto-link behavior. */
class SafeAboutLinkSpan(val destination: String) : URLSpan(destination) {
    override fun onClick(widget: android.view.View) {
        val context = widget.context
        AboutLinkActivation.activate(context, destination)
    }
}

internal fun configureAboutText(view: TextView) {
    view.text = ABOUT_CONTENT
    Linkify.addLinks(view, Linkify.ALL)
    val text = view.text
    if (text is Spannable) {
        text.getSpans(0, text.length, URLSpan::class.java).forEach { span ->
            val replacement = SafeAboutLinkSpan(span.url)
            text.setSpan(replacement, text.getSpanStart(span), text.getSpanEnd(span), text.getSpanFlags(span))
            text.removeSpan(span)
        }
    }
    view.linksClickable = true
    view.movementMethod = LinkMovementMethod.getInstance()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = { TopAppBar(title = { Text("关于App") }) },
    ) { insets ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(16.dp)
                .testTag("about-content"),
            factory = { context ->
                TextView(context).apply {
                    setLineSpacing(10f, 1f)
                    setTextColor(colorScheme.onSurface.toArgb())
                    setLinkTextColor(colorScheme.primary.toArgb())
                    configureAboutText(this)
                }
            },
            update = { view ->
                view.setTextColor(colorScheme.onSurface.toArgb())
                view.setLinkTextColor(colorScheme.primary.toArgb())
                configureAboutText(view)
            },
        )
    }
}
