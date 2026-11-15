package com.termux.app.tv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.termux.app.TermuxService
import com.termux.terminal.TerminalSession

/**
 * Coordinates Termux's service-backed session list with the Jetpack Compose for TV
 * [TerminalDrawerPanel] hosted inside [com.termux.app.TermuxActivity]'s DrawerLayout.
 */
class TvDrawerBridge(
    private val onSelectSession: (Int) -> Unit,
    private val onRenameSession: (Int) -> Unit,
    private val onCloseSession: (Int) -> Unit,
    private val onNewSession: () -> Unit,
    private val onNewNamedSession: () -> Unit,
    private val onToggleKeyboard: () -> Unit,
    private val onToggleToolbar: () -> Unit,
    private val onOpenSettings: () -> Unit,
    private val onCloseDrawer: () -> Unit
) {
    var sessions by mutableStateOf<List<TvSessionItem>>(emptyList())
        private set

    var focusTrigger by mutableIntStateOf(0)
        private set

    var targetScrollIndex by mutableStateOf<Int?>(null)
        private set

    fun bind(composeView: ComposeView) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            TermuxTvTheme {
                TerminalDrawerPanel(
                    sessions = sessions,
                    focusTrigger = focusTrigger,
                    targetScrollIndex = targetScrollIndex,
                    onConsumeScrollIndex = { targetScrollIndex = null },
                    onSelectSession = onSelectSession,
                    onRenameSession = onRenameSession,
                    onCloseSession = onCloseSession,
                    onNewSession = onNewSession,
                    onNewNamedSession = onNewNamedSession,
                    onToggleKeyboard = onToggleKeyboard,
                    onToggleToolbar = onToggleToolbar,
                    onOpenSettings = onOpenSettings,
                    onCloseDrawer = onCloseDrawer
                )
            }
        }
    }

    fun refresh(service: TermuxService?, currentSession: TerminalSession?) {
        val rawSessions = service?.termuxSessions ?: emptyList()
        sessions = TvPreferencesStore.mapSessionItems(
            sessions = rawSessions,
            currentHandle = currentSession?.mHandle
        )
    }

    fun notifyDrawerOpened() {
        focusTrigger++
    }

    fun scrollToSession(index: Int) {
        if (index >= 0) {
            targetScrollIndex = index
        }
    }
}
