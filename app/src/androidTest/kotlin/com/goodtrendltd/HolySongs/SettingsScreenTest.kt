package com.goodtrendltd.HolySongs

import android.content.Intent
import android.widget.Switch
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()
    @Test
    fun legacyPreferenceContractAndChoicesRemainExplicit() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("appPrefFile", context.getString(R.string.app_pref))
        assertEquals("fontSize", context.getString(R.string.font_size_pref_key))
        assertEquals("nightMode", context.getString(R.string.night_mode_pref_key))
        assertEquals(listOf("16", "18", "20", "22", "24", "26", "28", "30"),
            context.resources.getStringArray(R.array.font_size_array).toList())
        assertEquals(20, ReaderPreferenceSnapshot(20, 20, true).effectiveFontSize)
    }

    @Test
    fun settingsUsesSingleScaffoldAndKeepsAllControlsReachable() {
        composeRule.setContent {
            com.goodtrendltd.HolySongs.ui.SettingsScreen(
                preferences = ReaderPreferenceSnapshot(20, 20, true),
                onFontSizeSelected = {},
                onResetFontSize = {},
                onNightModeChanged = {},
            )
        }
        composeRule.onNodeWithText("设置").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-content").assertIsDisplayed()
        composeRule.onNodeWithText("字体大小").assertIsDisplayed()
        composeRule.onNodeWithText("恢复默认").assertIsDisplayed()
        composeRule.onNodeWithText("夜间模式(黑色背景)").assertIsDisplayed()
        composeRule.onNodeWithTag("night-mode-switch").assertIsDisplayed()
    }

    @Test
    fun settingsDisplaysEffectiveFontSizeWithoutUsingRawInvalidValue() {
        composeRule.setContent {
            com.goodtrendltd.HolySongs.ui.SettingsScreen(
                preferences = ReaderPreferenceSnapshot(201, 20, true),
                onFontSizeSelected = {},
                onResetFontSize = {},
                onNightModeChanged = {},
            )
        }
        composeRule.onNodeWithTag("font-size-current").assertTextContains("当前为: 20", substring = true)
    }

    @Test
    fun settingsActivityExposesTheHistoricalSwitchBoundary() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, SettingsActivity::class.java)
        ActivityScenario.launch<SettingsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity.findViewById<Switch>(R.id.nightModeSwitch))
            }
        }
    }
}
