package com.termux.app.tv

import android.content.Context
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.termux.R
import com.termux.app.models.UserAction
import com.termux.shared.activities.ReportActivity
import com.termux.shared.android.AndroidUtils
import com.termux.shared.file.FileUtils
import com.termux.shared.interact.ShareUtils
import com.termux.shared.models.ReportInfo
import com.termux.shared.termux.TermuxConstants
import com.termux.shared.termux.TermuxUtils
import com.termux.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxFloatAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences
import kotlin.concurrent.thread

private data class ActiveLogLevelPicker(
    val currentLevel: Int,
    val onSelect: (Int) -> Unit
)

object TvSettingsHost {
    @JvmStatic
    fun attach(activity: ComponentActivity) {
        activity.setContent {
            TermuxTvTheme {
                TvSettingsScreen(onBack = { activity.finish() })
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { TermuxAppSharedPreferences.build(context, false) }
    val categories = remember(context) { TvPreferencesStore.buildAvailableCategories(context) }
    val logLevelOptions = remember(context) { TvPreferencesStore.buildLogLevelOptions(context) }
    val showDonate = remember(context) { TvPreferencesStore.shouldShowDonate(context) }

    var selectedSection by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.section ?: SettingsSection.TERMUX_DEBUGGING)
    }
    var refreshRevision by remember { mutableIntStateOf(0) }
    var activePicker by remember { mutableStateOf<ActiveLogLevelPicker?>(null) }

    val initialRailFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        initialRailFocus.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Back || event.key == Key.Escape)
                ) {
                    if (activePicker != null) {
                        activePicker = null
                    } else {
                        onBack()
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_cancel)
                    )
                }
                Text(
                    text = stringResource(R.string.title_activity_termux_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .focusRestorer(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(categories, key = { _, item -> item.section.name }) { idx, item ->
                        val mod = if (idx == 0) {
                            Modifier.focusRequester(initialRailFocus)
                        } else {
                            Modifier
                        }
                        ListItem(
                            selected = selectedSection == item.section,
                            onClick = { selectedSection = item.section },
                            modifier = mod,
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            headlineContent = {
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (selectedSection) {
                        SettingsSection.TERMUX_DEBUGGING -> {
                            val currentLevel = remember(refreshRevision, prefs) {
                                prefs?.logLevel ?: 1
                            }
                            val toggles = remember(refreshRevision, prefs) {
                                if (prefs != null) {
                                    TvPreferencesStore.buildDebuggingToggles(context, prefs)
                                } else {
                                    emptyList()
                                }
                            }
                            SettingsToggleAndPickerPane(
                                currentLogLevelLabel = logLevelOptions.firstOrNull {
                                    it.value == currentLevel.toString()
                                }?.label ?: currentLevel.toString(),
                                onOpenLogLevel = if (prefs != null) {
                                    {
                                        activePicker = ActiveLogLevelPicker(currentLevel) { level ->
                                            TvPreferencesStore.setTermuxLogLevel(context, prefs, level)
                                            refreshRevision++
                                        }
                                    }
                                } else null,
                                toggles = toggles,
                                onToggle = { toggle ->
                                    if (prefs != null) {
                                        TvPreferencesStore.setBooleanPreference(
                                            prefs,
                                            toggle.key,
                                            !toggle.checked
                                        )
                                        refreshRevision++
                                    }
                                }
                            )
                        }

                        SettingsSection.TERMUX_TERMINAL_IO -> {
                            val toggles = remember(refreshRevision, prefs) {
                                if (prefs != null) {
                                    TvPreferencesStore.buildTerminalIoToggles(context, prefs)
                                } else {
                                    emptyList()
                                }
                            }
                            SettingsToggleAndPickerPane(
                                currentLogLevelLabel = null,
                                onOpenLogLevel = null,
                                toggles = toggles,
                                onToggle = { toggle ->
                                    if (prefs != null) {
                                        TvPreferencesStore.setBooleanPreference(
                                            prefs,
                                            toggle.key,
                                            !toggle.checked
                                        )
                                        refreshRevision++
                                    }
                                }
                            )
                        }

                        SettingsSection.TERMUX_TERMINAL_VIEW -> {
                            val toggles = remember(refreshRevision, prefs) {
                                if (prefs != null) {
                                    TvPreferencesStore.buildTerminalViewToggles(context, prefs)
                                } else {
                                    emptyList()
                                }
                            }
                            SettingsToggleAndPickerPane(
                                currentLogLevelLabel = null,
                                onOpenLogLevel = null,
                                toggles = toggles,
                                onToggle = { toggle ->
                                    if (prefs != null) {
                                        TvPreferencesStore.setBooleanPreference(
                                            prefs,
                                            toggle.key,
                                            !toggle.checked
                                        )
                                        refreshRevision++
                                    }
                                }
                            )
                        }

                        SettingsSection.ADDON_API,
                        SettingsSection.ADDON_FLOAT,
                        SettingsSection.ADDON_TASKER,
                        SettingsSection.ADDON_WIDGET -> {
                            val currentLevel = remember(selectedSection, refreshRevision) {
                                readAddonLogLevel(context, selectedSection)
                            }
                            SettingsToggleAndPickerPane(
                                currentLogLevelLabel = logLevelOptions.firstOrNull {
                                    it.value == currentLevel.toString()
                                }?.label ?: currentLevel.toString(),
                                onOpenLogLevel = {
                                    activePicker = ActiveLogLevelPicker(currentLevel) { level ->
                                        writeAddonLogLevel(context, selectedSection, level)
                                        refreshRevision++
                                    }
                                },
                                toggles = emptyList(),
                                onToggle = {}
                            )
                        }

                        SettingsSection.ABOUT -> {
                            AboutPane(
                                showDonate = showDonate,
                                onOpenReport = { launchAboutReport(context) },
                                onOpenDonate = {
                                    ShareUtils.openUrl(context, TermuxConstants.TERMUX_DONATE_URL)
                                }
                            )
                        }
                    }
                }
            }
        }

        val picker = activePicker
        if (picker != null) {
            LogLevelPickerOverlay(
                options = logLevelOptions,
                selectedValue = picker.currentLevel.toString(),
                onSelect = { level ->
                    picker.onSelect(level)
                    activePicker = null
                },
                onDismiss = { activePicker = null }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SettingsToggleAndPickerPane(
    currentLogLevelLabel: String?,
    onOpenLogLevel: (() -> Unit)?,
    toggles: List<ToggleSettingState>,
    onToggle: (ToggleSettingState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .focusRestorer(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentLogLevelLabel != null && onOpenLogLevel != null) {
            item(key = "log_level") {
                ListItem(
                    selected = false,
                    onClick = onOpenLogLevel,
                    modifier = Modifier.widthIn(max = 600.dp),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    headlineContent = {
                        Text(text = stringResource(R.string.termux_log_level_title))
                    },
                    supportingContent = {
                        Text(
                            text = currentLogLevelLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        items(toggles, key = { it.key }) { toggle ->
            ListItem(
                selected = toggle.checked,
                onClick = { onToggle(toggle) },
                modifier = Modifier.widthIn(max = 600.dp),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                headlineContent = {
                    Text(text = toggle.title)
                },
                supportingContent = {
                    Text(
                        text = toggle.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingContent = {
                    Switch(
                        checked = toggle.checked,
                        onCheckedChange = null
                    )
                }
            )
        }
    }
}

@Composable
private fun AboutPane(
    showDonate: Boolean,
    onOpenReport: () -> Unit,
    onOpenDonate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.widthIn(max = 600.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.about_preference_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.tv_settings_about_description),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onOpenReport) {
                    Text(text = stringResource(R.string.tv_settings_open_report))
                }
                if (showDonate) {
                    OutlinedButton(onClick = onOpenDonate) {
                        Text(text = stringResource(R.string.donate_preference_title))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LogLevelPickerOverlay(
    options: List<PickerOption>,
    selectedValue: String,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedFocus = remember { FocusRequester() }
    LaunchedEffect(selectedValue) {
        selectedFocus.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.tv_settings_choose_value),
                    style = MaterialTheme.typography.titleLarge
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .focusRestorer(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(options, key = { _, option -> option.value }) { index, option ->
                        val isSelected = option.value == selectedValue
                        val itemMod = if (isSelected || (options.none { it.value == selectedValue } && index == 0)) {
                            Modifier.focusRequester(selectedFocus)
                        } else {
                            Modifier
                        }
                        ListItem(
                            selected = isSelected,
                            onClick = {
                                val parsed = option.value.toIntOrNull()
                                if (parsed != null) onSelect(parsed)
                            },
                            modifier = itemMod,
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            headlineContent = {
                                Text(text = option.label)
                            }
                        )
                    }
                }
                OutlinedButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

private fun readAddonLogLevel(context: Context, section: SettingsSection): Int {
    return when (section) {
        SettingsSection.ADDON_API -> TermuxAPIAppSharedPreferences.build(context, false)?.getLogLevel(true) ?: 1
        SettingsSection.ADDON_FLOAT -> TermuxFloatAppSharedPreferences.build(context, false)?.getLogLevel(true) ?: 1
        SettingsSection.ADDON_TASKER -> TermuxTaskerAppSharedPreferences.build(context, false)?.getLogLevel(true) ?: 1
        SettingsSection.ADDON_WIDGET -> TermuxWidgetAppSharedPreferences.build(context, false)?.getLogLevel(true) ?: 1
        else -> 1
    }
}

private fun writeAddonLogLevel(context: Context, section: SettingsSection, level: Int) {
    when (section) {
        SettingsSection.ADDON_API -> TermuxAPIAppSharedPreferences.build(context, false)?.setLogLevel(context, level, true)
        SettingsSection.ADDON_FLOAT -> TermuxFloatAppSharedPreferences.build(context, false)?.setLogLevel(context, level, true)
        SettingsSection.ADDON_TASKER -> TermuxTaskerAppSharedPreferences.build(context, false)?.setLogLevel(context, level, true)
        SettingsSection.ADDON_WIDGET -> TermuxWidgetAppSharedPreferences.build(context, false)?.setLogLevel(context, level, true)
        else -> Unit
    }
}

private fun launchAboutReport(context: Context) {
    thread {
        val title = context.getString(R.string.about_preference_title)
        val aboutString = StringBuilder()
        aboutString.append(
            TermuxUtils.getAppInfoMarkdownString(
                context,
                TermuxUtils.AppInfoMode.TERMUX_AND_PLUGIN_PACKAGES
            )
        )
        aboutString.append("\n\n").append(AndroidUtils.getDeviceInfoMarkdownString(context, true))
        aboutString.append("\n\n").append(TermuxUtils.getImportantLinksMarkdownString(context))

        val userActionName = UserAction.ABOUT.getName()
        val reportInfo = ReportInfo(
            userActionName,
            TermuxConstants.TERMUX_APP.TERMUX_SETTINGS_ACTIVITY_NAME,
            title
        )
        reportInfo.setReportString(aboutString.toString())
        reportInfo.setReportSaveFileLabelAndPath(
            userActionName,
            Environment.getExternalStorageDirectory().toString() + "/" +
                FileUtils.sanitizeFileName(
                    TermuxConstants.TERMUX_APP_NAME + "-" + userActionName + ".log",
                    true,
                    true
                )
        )
        ReportActivity.startReportActivity(context, reportInfo)
    }
}
