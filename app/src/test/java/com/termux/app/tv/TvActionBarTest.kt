package com.termux.app.tv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvActionBarTest {

    @Test
    fun scrollMode_togglesAndResetsCleanly() {
        val bridge = TvActionBarBridge(
            onOpenDrawer = {},
            onShowKeyboard = {},
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
    }

    @Test
    fun requestBarFocus_incrementsFocusTrigger() {
        val bridge = TvActionBarBridge(
            onOpenDrawer = {},
            onShowKeyboard = {},
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
}
