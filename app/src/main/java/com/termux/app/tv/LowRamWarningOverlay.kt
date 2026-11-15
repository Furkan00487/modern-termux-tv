package com.termux.app.tv

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.termux.R

object LowRamWarningHost {
    @JvmStatic
    fun show(
        composeView: ComposeView,
        formattedMemory: String,
        onDismiss: () -> Unit,
        onDontShowAgain: () -> Unit
    ) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.visibility = View.VISIBLE
        composeView.setContent {
            TermuxTvTheme {
                LowRamWarningOverlay(
                    formattedMemory = formattedMemory,
                    onDismiss = {
                        hide(composeView)
                        onDismiss()
                    },
                    onDontShowAgain = {
                        hide(composeView)
                        onDontShowAgain()
                    }
                )
            }
        }
        composeView.requestFocus()
    }

    @JvmStatic
    fun hide(composeView: ComposeView?) {
        if (composeView == null) return
        composeView.setContent {}
        composeView.visibility = View.GONE
    }
}

@Composable
fun LowRamWarningOverlay(
    formattedMemory: String,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit
) {
    val okFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        okFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .padding(48.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Back || event.key == Key.Escape)
                ) {
                    onDismiss()
                    true
                } else {
                    false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_low_ram_warning),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = stringResource(R.string.msg_low_ram_warning, formattedMemory),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(max = 560.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.focusRequester(okFocusRequester)
                    ) {
                        Text(text = stringResource(R.string.action_ok))
                    }

                    OutlinedButton(onClick = onDontShowAgain) {
                        Text(text = stringResource(R.string.action_dont_show_again))
                    }
                }
            }
        }
    }
}
