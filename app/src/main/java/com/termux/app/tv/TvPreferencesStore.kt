package com.termux.app.tv

import android.content.Context
import com.termux.R
import com.termux.app.ExitKillPolicy
import com.termux.shared.android.PackageUtils
import com.termux.shared.logger.Logger
import com.termux.shared.termux.TermuxConstants
import com.termux.shared.termux.TermuxUtils
import com.termux.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxFloatAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession

/**
 * State and preference bridge connecting Compose for TV screens to Termux's
 * existing multi-process shared preference stores and runtime policies.
 */
object TvPreferencesStore {

    fun buildAvailableCategories(context: Context): List<SettingsCategoryItem> {
        val items = mutableListOf(
            SettingsCategoryItem(
                SettingsSection.TERMUX_DEBUGGING,
                context.getString(R.string.termux_debugging_preferences_title),
                context.getString(R.string.termux_debugging_preferences_summary)
            ),
            SettingsCategoryItem(
                SettingsSection.TERMUX_TERMINAL_IO,
                context.getString(R.string.termux_terminal_io_preferences_title),
                context.getString(R.string.termux_terminal_io_preferences_summary)
            ),
            SettingsCategoryItem(
                SettingsSection.TERMUX_TERMINAL_VIEW,
                context.getString(R.string.termux_terminal_view_preferences_title),
                context.getString(R.string.termux_terminal_view_preferences_summary)
            )
        )

        if (TermuxAPIAppSharedPreferences.build(context, false) != null) {
            items += SettingsCategoryItem(
                SettingsSection.ADDON_API,
                context.getString(R.string.termux_api_preferences_title),
                context.getString(R.string.termux_api_preferences_summary)
            )
        }
        if (TermuxFloatAppSharedPreferences.build(context, false) != null) {
            items += SettingsCategoryItem(
                SettingsSection.ADDON_FLOAT,
                context.getString(R.string.termux_float_preferences_title),
                context.getString(R.string.termux_float_preferences_summary)
            )
        }
        if (TermuxTaskerAppSharedPreferences.build(context, false) != null) {
            items += SettingsCategoryItem(
                SettingsSection.ADDON_TASKER,
                context.getString(R.string.termux_tasker_preferences_title),
                context.getString(R.string.termux_tasker_preferences_summary)
            )
        }
        if (TermuxWidgetAppSharedPreferences.build(context, false) != null) {
            items += SettingsCategoryItem(
                SettingsSection.ADDON_WIDGET,
                context.getString(R.string.termux_widget_preferences_title),
                context.getString(R.string.termux_widget_preferences_summary)
            )
        }

        items += SettingsCategoryItem(
            SettingsSection.ABOUT,
            context.getString(R.string.about_preference_title),
            context.getString(R.string.tv_settings_about_summary)
        )
        return items
    }

    fun buildDebuggingToggles(
        context: Context,
        prefs: TermuxAppSharedPreferences,
        isTvDevice: Boolean = ExitKillPolicy.isTelevisionDevice(context)
    ): List<ToggleSettingState> {
        val keyLogging = prefs.isTerminalViewKeyLoggingEnabled()
        val pluginErrors = prefs.arePluginErrorNotificationsEnabled(false)
        val crashReports = prefs.areCrashReportNotificationsEnabled(false)
        val exitKill = prefs.isExitKillProcessEnabled(isTvDevice)

        return listOf(
            ToggleSettingState(
                key = TvPreferenceKeys.EXIT_KILL_PROCESS,
                title = context.getString(R.string.termux_exit_kill_process_title),
                summary = context.getString(
                    if (exitKill) R.string.termux_exit_kill_process_on
                    else R.string.termux_exit_kill_process_off
                ),
                checked = exitKill
            ),
            ToggleSettingState(
                key = TvPreferenceKeys.TERMINAL_VIEW_KEY_LOGGING,
                title = context.getString(R.string.termux_terminal_view_key_logging_enabled_title),
                summary = context.getString(
                    if (keyLogging) R.string.termux_terminal_view_key_logging_enabled_on
                    else R.string.termux_terminal_view_key_logging_enabled_off
                ),
                checked = keyLogging
            ),
            ToggleSettingState(
                key = TvPreferenceKeys.PLUGIN_ERROR_NOTIFICATIONS,
                title = context.getString(R.string.termux_plugin_error_notifications_enabled_title),
                summary = context.getString(
                    if (pluginErrors) R.string.termux_plugin_error_notifications_enabled_on
                    else R.string.termux_plugin_error_notifications_enabled_off
                ),
                checked = pluginErrors
            ),
            ToggleSettingState(
                key = TvPreferenceKeys.CRASH_REPORT_NOTIFICATIONS,
                title = context.getString(R.string.termux_crash_report_notifications_enabled_title),
                summary = context.getString(
                    if (crashReports) R.string.termux_crash_report_notifications_enabled_on
                    else R.string.termux_crash_report_notifications_enabled_off
                ),
                checked = crashReports
            )
        )
    }

    fun buildTerminalIoToggles(
        context: Context,
        prefs: TermuxAppSharedPreferences
    ): List<ToggleSettingState> {
        val softKeyboard = prefs.isSoftKeyboardEnabled()
        val onlyIfNoHardware = prefs.isSoftKeyboardEnabledOnlyIfNoHardware()

        return listOf(
            ToggleSettingState(
                key = TvPreferenceKeys.SOFT_KEYBOARD_ENABLED,
                title = context.getString(R.string.termux_soft_keyboard_enabled_title),
                summary = context.getString(
                    if (softKeyboard) R.string.termux_soft_keyboard_enabled_on
                    else R.string.termux_soft_keyboard_enabled_off
                ),
                checked = softKeyboard
            ),
            ToggleSettingState(
                key = TvPreferenceKeys.SOFT_KEYBOARD_ONLY_IF_NO_HARDWARE,
                title = context.getString(R.string.termux_soft_keyboard_enabled_only_if_no_hardware_title),
                summary = context.getString(
                    if (onlyIfNoHardware) R.string.termux_soft_keyboard_enabled_only_if_no_hardware_on
                    else R.string.termux_soft_keyboard_enabled_only_if_no_hardware_off
                ),
                checked = onlyIfNoHardware
            )
        )
    }

    fun buildTerminalViewToggles(
        context: Context,
        prefs: TermuxAppSharedPreferences
    ): List<ToggleSettingState> {
        val marginAdjustment = prefs.isTerminalMarginAdjustmentEnabled()
        return listOf(
            ToggleSettingState(
                key = TvPreferenceKeys.TERMINAL_MARGIN_ADJUSTMENT,
                title = context.getString(R.string.termux_terminal_view_terminal_margin_adjustment_title),
                summary = context.getString(
                    if (marginAdjustment) R.string.termux_terminal_view_terminal_margin_adjustment_on
                    else R.string.termux_terminal_view_terminal_margin_adjustment_off
                ),
                checked = marginAdjustment
            )
        )
    }

    fun setBooleanPreference(
        prefs: TermuxAppSharedPreferences,
        key: String,
        value: Boolean
    ) {
        when (key) {
            TvPreferenceKeys.TERMINAL_VIEW_KEY_LOGGING -> prefs.setTerminalViewKeyLoggingEnabled(value)
            TvPreferenceKeys.PLUGIN_ERROR_NOTIFICATIONS -> prefs.setPluginErrorNotificationsEnabled(value)
            TvPreferenceKeys.CRASH_REPORT_NOTIFICATIONS -> prefs.setCrashReportNotificationsEnabled(value)
            TvPreferenceKeys.EXIT_KILL_PROCESS -> prefs.setExitKillProcessEnabled(value)
            TvPreferenceKeys.SOFT_KEYBOARD_ENABLED -> prefs.setSoftKeyboardEnabled(value)
            TvPreferenceKeys.SOFT_KEYBOARD_ONLY_IF_NO_HARDWARE -> prefs.setSoftKeyboardEnabledOnlyIfNoHardware(value)
            TvPreferenceKeys.TERMINAL_MARGIN_ADJUSTMENT -> prefs.setTerminalMarginAdjustment(value)
        }
    }

    fun buildLogLevelOptions(context: Context): List<PickerOption> {
        val values = Logger.getLogLevelsArray()
        val labels = Logger.getLogLevelLabelsArray(context, values, true)
        return values.indices.map { i ->
            PickerOption(
                value = values[i].toString(),
                label = labels[i].toString()
            )
        }
    }

    fun setTermuxLogLevel(
        context: Context,
        prefs: TermuxAppSharedPreferences,
        logLevel: Int
    ) {
        prefs.setLogLevel(context, logLevel)
    }

    fun shouldShowDonate(context: Context): Boolean {
        val digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(context) ?: return false
        val apkRelease = TermuxUtils.getAPKRelease(digest)
        return apkRelease != null &&
            apkRelease != TermuxConstants.APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST
    }

    fun mapSessionItems(
        sessions: List<TermuxSession>,
        currentHandle: String?
    ): List<TvSessionItem> {
        return sessions.mapIndexedNotNull { index, termuxSession ->
            val session = termuxSession.terminalSession ?: return@mapIndexedNotNull null
            TvSessionItem(
                handle = session.mHandle,
                index = index,
                displayTitle = formatSessionDisplayTitle(index, session.mSessionName, session.title),
                isCurrent = session.mHandle == currentHandle,
                isRunning = session.isRunning,
                exitStatus = session.exitStatus
            )
        }
    }

    fun formatSessionDisplayTitle(index: Int, name: String?, title: String?): String {
        val cleanName = name?.trim().orEmpty()
        val cleanTitle = title?.trim().orEmpty()
        val label = when {
            cleanName.isNotEmpty() && cleanTitle.isNotEmpty() -> "$cleanName — $cleanTitle"
            cleanName.isNotEmpty() -> cleanName
            cleanTitle.isNotEmpty() -> cleanTitle
            else -> "Session"
        }
        return "[${index + 1}] $label"
    }
}
