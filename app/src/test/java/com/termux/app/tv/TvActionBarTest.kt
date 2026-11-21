package com.termux.app.tv

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvActionBarTest {

    @Test
    fun scrollMode_togglesAndResetsCleanly() {
        var extraKeysFocused = false
        val bridge = TvActionBarBridge(
            onOpenDrawer = {},
            onShowKeyboard = {},
            onFocusExtraKeys = { extraKeysFocused = true },
            onScrollUp = {},
            onScrollDown = {},
            onOpenContextMenu = {},
            onOpenSettings = {},
            onReturnToTerminal = {}
        )

        assertFalse(bridge.scrollMode)
        assertTrue(bridge.toggleScrollMode())
        assertTrue(bridge.scrollMode)
        assertFalse(bridge.toggleScrollMode())
        assertFalse(bridge.scrollMode)

        bridge.setScrollModeEnabled(true)
        assertTrue(bridge.scrollMode)
        bridge.setScrollModeEnabled(false)
        assertFalse(bridge.scrollMode)
        assertFalse(extraKeysFocused)
    }

    @Test
    fun requestBarFocus_incrementsFocusTrigger() {
        val bridge = TvActionBarBridge(
            onOpenDrawer = {},
            onShowKeyboard = {},
            onFocusExtraKeys = {},
            onScrollUp = {},
            onScrollDown = {},
            onOpenContextMenu = {},
            onOpenSettings = {},
            onReturnToTerminal = {}
        )

        assertEquals(0, bridge.focusTrigger)
        bridge.requestBarFocus()
        assertEquals(1, bridge.focusTrigger)
        bridge.requestBarFocus()
        assertEquals(2, bridge.focusTrigger)
    }

    @Test
    fun longPressGuard_suppressesOrphanUpAfterLongPressWithoutAffectingShortPress() {
        val guard = TvDpadLongPressGuard()

        // Short press should never be suppressed
        assertFalse(guard.suppressNextCenterUp)
        assertFalse(guard.consumePostLongPressEvent(KeyEvent.ACTION_DOWN))
        assertFalse(guard.consumePostLongPressEvent(KeyEvent.ACTION_UP))

        // Long press triggers while key is still held down -> repeat downs and orphan up must be consumed
        guard.onLongPressTriggered()
        assertTrue(guard.suppressNextCenterUp)
        assertTrue(guard.consumePostLongPressEvent(KeyEvent.ACTION_DOWN))
        assertTrue(guard.suppressNextCenterUp)
        assertTrue(guard.consumePostLongPressEvent(KeyEvent.ACTION_UP))

        // After orphan ACTION_UP is consumed, subsequent short press in action bar flows through
        assertFalse(guard.suppressNextCenterUp)
        assertFalse(guard.consumePostLongPressEvent(KeyEvent.ACTION_DOWN))
        assertFalse(guard.consumePostLongPressEvent(KeyEvent.ACTION_UP))
    }

    @Test
    fun dpadCenterPolicy_sendsEnterForTuiOrVisibleKeyboardAndOpensKeyboardAtPrompt() {
        // Normal shell prompt with keyboard hidden -> open soft keyboard
        assertFalse(
            TvDpadCenterPolicy.shouldSendEnter(
                isAlternateBufferActive = false,
                isMouseTrackingActive = false,
                isSoftKeyboardVisible = false
            )
        )

        // Alternate-buffer TUIs (vim, nano, htop) -> send Enter
        assertTrue(
            TvDpadCenterPolicy.shouldSendEnter(
                isAlternateBufferActive = true,
                isMouseTrackingActive = false,
                isSoftKeyboardVisible = false
            )
        )

        // Main-buffer TUIs with mouse tracking (e.g. ncurses dialog) -> send Enter
        assertTrue(
            TvDpadCenterPolicy.shouldSendEnter(
                isAlternateBufferActive = false,
                isMouseTrackingActive = true,
                isSoftKeyboardVisible = false
            )
        )

        // Normal shell prompt when soft keyboard is already visible -> send Enter
        assertTrue(
            TvDpadCenterPolicy.shouldSendEnter(
                isAlternateBufferActive = false,
                isMouseTrackingActive = false,
                isSoftKeyboardVisible = true
            )
        )
    }
}
