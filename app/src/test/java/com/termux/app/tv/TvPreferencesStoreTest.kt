package com.termux.app.tv

import android.app.Application
import android.content.Context
import com.termux.R
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class TvPreferencesStoreTest {

    @Test
    fun buildTermuxDebuggingToggles_usesDeviceDefaultForExitKillAndPersistsToggles() {
        val context: Context = RuntimeEnvironment.getApplication()
        val prefs = TermuxAppSharedPreferences.build(context, false)
            ?: error("Expected TermuxAppSharedPreferences instance")

        val tvToggles = TvPreferencesStore.buildDebuggingToggles(context, prefs, isTvDevice = true)
        val phoneToggles = TvPreferencesStore.buildDebuggingToggles(context, prefs, isTvDevice = false)

        val tvExitToggle = tvToggles.first { it.key == TvPreferenceKeys.EXIT_KILL_PROCESS }
        val phoneExitToggle = phoneToggles.first { it.key == TvPreferenceKeys.EXIT_KILL_PROCESS }

        assertTrue(tvExitToggle.checked)
        assertFalse(phoneExitToggle.checked)

        TvPreferencesStore.setBooleanPreference(prefs, TvPreferenceKeys.EXIT_KILL_PROCESS, false)
        TvPreferencesStore.setBooleanPreference(prefs, TvPreferenceKeys.TERMINAL_VIEW_KEY_LOGGING, true)

        val updated = TvPreferencesStore.buildDebuggingToggles(context, prefs, isTvDevice = true)
        assertFalse(updated.first { it.key == TvPreferenceKeys.EXIT_KILL_PROCESS }.checked)
        assertTrue(updated.first { it.key == TvPreferenceKeys.TERMINAL_VIEW_KEY_LOGGING }.checked)
    }

    @Test
    fun buildTerminalIoAndViewToggles_roundTripsPreferences() {
        val context: Context = RuntimeEnvironment.getApplication()
        val prefs = TermuxAppSharedPreferences.build(context, false)
            ?: error("Expected TermuxAppSharedPreferences instance")

        TvPreferencesStore.setBooleanPreference(prefs, TvPreferenceKeys.SOFT_KEYBOARD_ENABLED, false)
        TvPreferencesStore.setBooleanPreference(prefs, TvPreferenceKeys.SOFT_KEYBOARD_ONLY_IF_NO_HARDWARE, true)
        TvPreferencesStore.setBooleanPreference(prefs, TvPreferenceKeys.TERMINAL_MARGIN_ADJUSTMENT, false)

        val ioToggles = TvPreferencesStore.buildTerminalIoToggles(context, prefs)
        val viewToggles = TvPreferencesStore.buildTerminalViewToggles(context, prefs)

        assertFalse(ioToggles.first { it.key == TvPreferenceKeys.SOFT_KEYBOARD_ENABLED }.checked)
        assertTrue(ioToggles.first { it.key == TvPreferenceKeys.SOFT_KEYBOARD_ONLY_IF_NO_HARDWARE }.checked)
        assertFalse(viewToggles.first { it.key == TvPreferenceKeys.TERMINAL_MARGIN_ADJUSTMENT }.checked)
    }

    @Test
    fun setTermuxLogLevel_updatesStoredLevelAndOptions() {
        val context: Context = RuntimeEnvironment.getApplication()
        val prefs = TermuxAppSharedPreferences.build(context, false)
            ?: error("Expected TermuxAppSharedPreferences instance")

        TvPreferencesStore.setTermuxLogLevel(context, prefs, 3)

        assertEquals(3, prefs.getLogLevel())
        val options = TvPreferencesStore.buildLogLevelOptions(context)
        assertTrue(options.any { it.value == "3" })
        assertTrue(R.string.termux_exit_kill_process_title != 0)
    }

    @Test
    fun formatSessionDisplayTitle_handlesNumberNameAndTitle() {
        assertEquals("[1] dev", TvPreferencesStore.formatSessionDisplayTitle(0, "dev", ""))
        assertEquals("[2] bash", TvPreferencesStore.formatSessionDisplayTitle(1, "", "bash"))
        assertEquals("[3] logs — tail -f", TvPreferencesStore.formatSessionDisplayTitle(2, "logs", "tail -f"))
        assertEquals("[1] Session", TvPreferencesStore.formatSessionDisplayTitle(0, null, null))
    }
}
