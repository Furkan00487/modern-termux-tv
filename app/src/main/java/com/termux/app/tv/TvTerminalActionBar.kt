package com.termux.app.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UnfoldMore
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.termux.R

class TvActionBarBridge(
    private val onOpenDrawer: () -> Unit,
    private val onShowKeyboard: () -> Unit,
    private val onScrollUp: () -> Unit,
    private val onScrollDown: () -> Unit,
    private val onOpenContextMenu: () -> Unit,
    private val onOpenSettings: () -> Unit,
    private val onReturnToTerminal: () -> Unit
) {
    var scrollMode by mutableStateOf(false)
        private set

    var focusTrigger by mutableIntStateOf(0)
        private set

    fun bind(composeView: ComposeView) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            TermuxTvTheme {
                TvTerminalActionBar(
                    scrollMode = scrollMode,
                    focusTrigger = focusTrigger,
                    onOpenDrawer = onOpenDrawer,
                    onShowKeyboard = onShowKeyboard,
                    onToggleScrollMode = { toggleScrollMode() },
                    onScrollUp = onScrollUp,
                    onScrollDown = onScrollDown,
                    onOpenContextMenu = onOpenContextMenu,
                    onOpenSettings = onOpenSettings,
                    onReturnToTerminal = onReturnToTerminal
                )
            }
        }
    }

    fun toggleScrollMode(): Boolean {
        scrollMode = !scrollMode
        return scrollMode
    }

    fun setScrollModeEnabled(enabled: Boolean) {
        scrollMode = enabled
    }

    fun requestBarFocus() {
        focusTrigger++
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvTerminalActionBar(
    scrollMode: Boolean,
    focusTrigger: Int,
    onOpenDrawer: () -> Unit,
    onShowKeyboard: () -> Unit,
    onToggleScrollMode: () -> Unit,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onOpenContextMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    onReturnToTerminal: () -> Unit
) {
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(focusTrigger) {
        if (focusTrigger > 0) {
            firstFocus.requestFocus()
        }
    }

    Surface(
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Back || event.key == Key.Escape || event.key == Key.DirectionDown)
                ) {
                    onReturnToTerminal()
                    true
                } else {
                    false
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .focusRestorer(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onOpenDrawer,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.focusRequester(firstFocus)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.tv_bar_sessions),
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onShowKeyboard,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_toggle_soft_keyboard),
                    maxLines = 1
                )
            }

            if (scrollMode) {
                Button(
                    onClick = onToggleScrollMode,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tv_bar_scroll_on),
                        maxLines = 1
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onToggleScrollMode,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tv_bar_scroll),
                        maxLines = 1
                    )
                }
            }

            OutlinedButton(
                onClick = onScrollUp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.tv_bar_scroll_up),
                    modifier = Modifier.size(18.dp)
                )
            }

            OutlinedButton(
                onClick = onScrollDown,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.tv_bar_scroll_down),
                    modifier = Modifier.size(18.dp)
                )
            }

            OutlinedButton(
                onClick = onOpenContextMenu,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.tv_bar_menu),
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onOpenSettings,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.action_open_settings),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (scrollMode) {
                    stringResource(R.string.tv_bar_hint_scroll_mode)
                } else {
                    stringResource(R.string.tv_bar_hint_default)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (scrollMode) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
