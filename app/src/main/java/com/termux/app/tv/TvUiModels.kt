package com.termux.app.tv

/** Keys matching [com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants]. */
object TvPreferenceKeys {
    const val TERMINAL_VIEW_KEY_LOGGING = "terminal_view_key_logging_enabled"
    const val PLUGIN_ERROR_NOTIFICATIONS = "plugin_error_notifications_enabled"
    const val CRASH_REPORT_NOTIFICATIONS = "crash_report_notifications_enabled"
    const val EXIT_KILL_PROCESS = "exit_kill_process_enabled"
    const val SOFT_KEYBOARD_ENABLED = "soft_keyboard_enabled"
    const val SOFT_KEYBOARD_ONLY_IF_NO_HARDWARE = "soft_keyboard_enabled_only_if_no_hardware"
    const val TERMINAL_MARGIN_ADJUSTMENT = "terminal_margin_adjustment"
}

enum class SettingsSection {
    TERMUX_DEBUGGING,
    TERMUX_TERMINAL_IO,
    TERMUX_TERMINAL_VIEW,
    ADDON_API,
    ADDON_FLOAT,
    ADDON_TASKER,
    ADDON_WIDGET,
    ABOUT
}

data class SettingsCategoryItem(
    val section: SettingsSection,
    val title: String,
    val subtitle: String
)

data class ToggleSettingState(
    val key: String,
    val title: String,
    val summary: String,
    val checked: Boolean
)

data class PickerOption(
    val value: String,
    val label: String
)

data class TvSessionItem(
    val handle: String,
    val index: Int,
    val displayTitle: String,
    val isCurrent: Boolean,
    val isRunning: Boolean,
    val exitStatus: Int
)
