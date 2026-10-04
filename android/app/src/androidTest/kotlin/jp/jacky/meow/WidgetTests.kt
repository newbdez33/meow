package jp.jacky.meow

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.jacky.meow.ui.MeowTheme
import jp.jacky.meow.widget.MeowWidgetProvider
import jp.jacky.meow.widget.WidgetConfigurationContent
import jp.jacky.meow.widget.WidgetSelection
import jp.jacky.meow.widget.WidgetSettings
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetConfigurationTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun auditionDoesNotSelectAndSaveCommitsBothFields() {
        var saved: WidgetSelection? = null
        val played = mutableListOf<Int>()
        rule.setContent { MeowTheme {
            WidgetConfigurationContent(WidgetSelection(), onAudition = { played += it }, onCancel = {}, onSave = { saved = it })
        } }
        rule.onNodeWithTag("widget-sound").performClick()
        rule.onNodeWithTag("audition-m_001").performClick()
        rule.runOnIdle { assertEquals(listOf(0), played); assertNull(saved) }
        rule.onNodeWithTag("sound-m_002").performClick()
        rule.onNodeWithTag("widget-character").performClick()
        rule.onNodeWithTag("character-black").performClick()
        rule.onNodeWithTag("widget-save").performClick()
        rule.runOnIdle { assertEquals(WidgetSelection("m_002", "black"), saved) }
    }

    @Test
    fun cancelDoesNotPersistDraftChanges() {
        var saves = 0
        var cancelled = false
        rule.setContent { MeowTheme {
            WidgetConfigurationContent(WidgetSelection("m_027", "ghost"), onAudition = {}, onCancel = { cancelled = true }, onSave = { saves++ })
        } }
        rule.onNodeWithTag("widget-character").performClick()
        rule.onNodeWithTag("character-black").performClick()
        rule.onNodeWithTag("widget-cancel").performClick()
        rule.runOnIdle { assertTrue(cancelled); assertEquals(0, saves) }
    }
}

@RunWith(AndroidJUnit4::class)
class WidgetSettingsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun saveRestoreAndDeleteKeepInstancesSeparate() {
        val preferences = context.getSharedPreferences("widget_test", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val settings = WidgetSettings(preferences)
        val first = WidgetSelection("m_027", "ghost")
        val second = WidgetSelection("m_002", "black")
        try {
            assertTrue(settings.save(1, first))
            assertTrue(settings.save(2, second))
            assertEquals(first, WidgetSettings(preferences).read(1))
            settings.restore(intArrayOf(1, 2), intArrayOf(2, 3))
            assertEquals(WidgetSelection(), settings.read(1))
            assertEquals(first, settings.read(2))
            assertEquals(second, settings.read(3))
            settings.delete(intArrayOf(2))
            assertEquals(WidgetSelection(), settings.read(2))
            assertEquals(second, settings.read(3))
            preferences.edit().putString("4", "removed|sleepy").commit()
            assertEquals(WidgetSelection("m_004", "sleepy"), settings.read(4))
        } finally { preferences.edit().clear().commit() }
    }

    @Test
    fun clickIntentsHaveIndependentIdentityAndAreImmutable() {
        val first = MeowWidgetProvider.playbackIntent(context, 91001)
        val second = MeowWidgetProvider.playbackIntent(context, 91002)
        try {
            assertNotEquals(first, second)
            assertEquals(first, MeowWidgetProvider.playbackIntent(context, 91001))
            if (android.os.Build.VERSION.SDK_INT >= 31) assertTrue(first.isImmutable)
        } finally { first.cancel(); second.cancel() }
    }
}

@RunWith(AndroidJUnit4::class)
class AudioPlaybackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val audio get() = (context.applicationContext as MeowApplication).audio

    @Test
    fun replacingPlaybackFinishesTheOldOwnerAndOldReleaseCannotStopTheNewOne() {
        val first = Any()
        val second = Any()
        var finished = 0
        instrumentation.runOnMainSync {
            try {
                audio.play(first, 0, onFinished = { finished++ })
                audio.play(second, 3)
                assertEquals(1, finished)
                audio.stop(first)
                assertEquals("m_004", audio.soundID)
                audio.stop(second)
                assertNull(audio.soundID)
            } finally { audio.stop() }
        }
    }

    @Test
    fun completionReleasesThePlayer() {
        instrumentation.runOnMainSync { audio.play(this, 3) }
        val deadline = System.currentTimeMillis() + 15_000
        var active = true
        while (active && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
            instrumentation.runOnMainSync { active = audio.soundID != null }
        }
        instrumentation.runOnMainSync {
            try { assertNull(audio.soundID) } finally { audio.stop() }
        }
    }
}
