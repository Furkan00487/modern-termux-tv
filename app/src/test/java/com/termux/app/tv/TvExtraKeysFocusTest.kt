package com.termux.app.tv

import android.app.Application
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.button.MaterialButton
import com.termux.R
import com.termux.shared.termux.extrakeys.ExtraKeysConstants
import com.termux.shared.termux.extrakeys.ExtraKeysInfo
import com.termux.shared.termux.extrakeys.ExtraKeysView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class TvExtraKeysFocusTest {

    @Test
    fun reload_setsFocusableGridBoundsAndBoundaryClamping() {
        val themedContext = ContextThemeWrapper(
            RuntimeEnvironment.getApplication(),
            R.style.Theme_TermuxActivity_DayNight_NoActionBar
        )
        val extraKeysView = ExtraKeysView(themedContext, null)
        val info = ExtraKeysInfo(
            "[[\"ESC\", \"TAB\", \"HOME\"], [\"CTRL\", \"ALT\", \"LEFT\"]]",
            "default",
            ExtraKeysConstants.CONTROL_CHARS_ALIASES
        )

        extraKeysView.reload(info, 80f)

        assertEquals(6, extraKeysView.childCount)
        val esc = extraKeysView.getChildAt(0) as MaterialButton
        val tab = extraKeysView.getChildAt(1) as MaterialButton
        val home = extraKeysView.getChildAt(2) as MaterialButton
        val ctrl = extraKeysView.getChildAt(3) as MaterialButton
        val alt = extraKeysView.getChildAt(4) as MaterialButton
        val left = extraKeysView.getChildAt(5) as MaterialButton

        assertTrue(esc.isFocusable)
        assertNotEquals(View.NO_ID, esc.id)

        // Row 0 horizontal navigation & clamping
        assertEquals(esc.id, esc.nextFocusLeftId)
        assertEquals(tab.id, esc.nextFocusRightId)
        assertEquals(home.id, tab.nextFocusRightId)
        assertEquals(home.id, home.nextFocusRightId)

        // Row 0 UP leaves View.NO_ID so focus returns cleanly to TerminalView
        assertEquals(View.NO_ID, esc.nextFocusUpId)

        // Vertical links between row 0 and row 1
        assertEquals(ctrl.id, esc.nextFocusDownId)
        assertEquals(esc.id, ctrl.nextFocusUpId)
        assertEquals(alt.id, tab.nextFocusDownId)
        assertEquals(tab.id, alt.nextFocusUpId)

        // Bottom row clamps DOWN so focus does not escape below toolbar
        assertEquals(ctrl.id, ctrl.nextFocusDownId)
        assertEquals(left.id, left.nextFocusDownId)
        assertEquals(left.id, left.nextFocusRightId)
    }

    @Test
    fun boundaryKeys_consumeEdgeDpadMovement() {
        val themedContext = ContextThemeWrapper(
            RuntimeEnvironment.getApplication(),
            R.style.Theme_TermuxActivity_DayNight_NoActionBar
        )
        val extraKeysView = ExtraKeysView(themedContext, null)
        val info = ExtraKeysInfo(
            "[[\"ESC\", \"HOME\"], [\"CTRL\", \"RIGHT\"]]",
            "default",
            ExtraKeysConstants.CONTROL_CHARS_ALIASES
        )

        extraKeysView.reload(info, 80f)

        val esc = extraKeysView.getChildAt(0) as MaterialButton
        val home = extraKeysView.getChildAt(1) as MaterialButton
        val ctrl = extraKeysView.getChildAt(2) as MaterialButton
        val right = extraKeysView.getChildAt(3) as MaterialButton

        assertTrue(esc.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)))
        assertTrue(home.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)))
        assertTrue(ctrl.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)))
        assertTrue(right.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)))
    }
}
