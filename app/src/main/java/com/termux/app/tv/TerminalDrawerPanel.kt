package com.termux.app.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.tv.material3.Text
import com.termux.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TerminalDrawerPanel(
    sessions: List<TvSessionItem>,
    focusTrigger: Int,
    targetScrollIndex: Int?,
    onConsumeScrollIndex: () -> Unit,
    onSelectSession: (Int) -> Unit,
    onRenameSession: (Int) -> Unit,
    onCloseSession: (Int) -> Unit,
    onNewSession: () -> Unit,
    onNewNamedSession: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onToggleToolbar: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val listState = rememberLazyListState()
    val primaryFocusRequester = remember { FocusRequester() }
    var actionTarget by remember { mutableStateOf<TvSessionItem?>(null) }

    LaunchedEffect(focusTrigger) {
        if (focusTrigger > 0) {
            primaryFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(targetScrollIndex) {
        val index = targetScrollIndex ?: return@LaunchedEffect
        if (index in sessions.indices) {
            listState.animateScrollToItem(index)
        }
        onConsumeScrollIndex()
    }

    Surface(
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Back || event.key == Key.Escape)
                ) {
                    if (actionTarget != null) {
                        actionTarget = null
                    } else {
                        onCloseDrawer()
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .focusProperties {
                    onExit = { cancelFocusChange() }
                }
                .focusGroup()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tv_drawer_sessions_header),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = if (sessions.isEmpty()) {
                            Modifier.focusRequester(primaryFocusRequester)
                        } else {
                            Modifier
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_open_settings)
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .focusRestorer(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions, key = { it.handle }) { item ->
                        val isInitialTarget = item.isCurrent || (sessions.none { it.isCurrent } && item.index == 0)
                        val rowModifier = if (isInitialTarget) {
                            Modifier.focusRequester(primaryFocusRequester)
                        } else {
                            Modifier
                        }
                        SessionDrawerRow(
                            item = item,
                            listItemModifier = rowModifier,
                            onSelect = { onSelectSession(item.index) },
                            onManage = { actionTarget = item }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNewSession,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.action_new_session),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleKeyboard,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.action_toggle_soft_keyboard),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = onToggleToolbar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tv_drawer_toolbar_action),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNewNamedSession,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.tv_drawer_named_session_action),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val selectedTarget = actionTarget
            if (selectedTarget != null) {
                SessionActionOverlay(
                    item = selectedTarget,
                    onSelect = {
                        actionTarget = null
                        onSelectSession(selectedTarget.index)
                    },
                    onRename = {
                        actionTarget = null
                        onRenameSession(selectedTarget.index)
                    },
                    onCloseSession = {
                        actionTarget = null
                        onCloseSession(selectedTarget.index)
                    },
                    onDismiss = { actionTarget = null }
                )
            }
        }
    }
}

@Composable
private fun SessionDrawerRow(
    item: TvSessionItem,
    listItemModifier: Modifier,
    onSelect: () -> Unit,
    onManage: () -> Unit
) {
    val failedExit = !item.isRunning && item.exitStatus != 0
    val statusText = if (item.isRunning) {
        stringResource(R.string.tv_drawer_session_running)
    } else {
        stringResource(R.string.tv_drawer_session_exited, item.exitStatus)
    }
    val textColor = when {
        failedExit -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ListItem(
            selected = item.isCurrent,
            onClick = onSelect,
            onLongClick = onManage,
            modifier = listItemModifier.weight(1f),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = textColor,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(
                    text = item.displayTitle,
                    fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.Medium,
                    textDecoration = if (item.isRunning) TextDecoration.None else TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        IconButton(onClick = onManage) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.tv_drawer_rename_action)
            )
        }
    }
}

@Composable
private fun SessionActionOverlay(
    item: TvSessionItem,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onCloseSession: () -> Unit,
    onDismiss: () -> Unit
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(item.handle) {
        firstFocus.requestFocus()
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusProperties {
                onExit = { cancelFocusChange() }
            }
            .focusGroup()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onRename,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(firstFocus)
            ) {
                Text(text = stringResource(R.string.tv_drawer_rename_action))
            }

            OutlinedButton(
                onClick = onCloseSession,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.tv_drawer_kill_action))
            }

            OutlinedButton(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.action_ok))
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    }
}
