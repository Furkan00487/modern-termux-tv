package com.termux.terminal;

import android.view.KeyEvent;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UpstreamTerminalFeaturesTest extends TerminalTestCase {

    @After
    public void resetBitmapLimits() {
        TerminalBitmap.resetImageLimits();
    }

    @Test
    public void testKeyHandlerPageUpAndPageDownTermcaps() {
        assertEquals(KeyHandler.getCode(KeyEvent.KEYCODE_PAGE_UP, 0, false, false),
            KeyHandler.getCodeFromTermcap("kP", false, false));
        assertEquals(KeyHandler.getCode(KeyEvent.KEYCODE_PAGE_DOWN, 0, false, false),
            KeyHandler.getCodeFromTermcap("kN", false, false));
        assertEquals("\033[5~", KeyHandler.getCodeFromTermcap("kP", false, false));
        assertEquals("\033[6~", KeyHandler.getCodeFromTermcap("kN", false, false));
    }

    @Test
    public void testTerminalSessionReadBufferIs64Kb() throws Exception {
        TerminalSession session = new TerminalSession("/bin/sh", "/", new String[0], new String[0], null, null);
        Field bufferField = ByteQueue.class.getDeclaredField("mBuffer");
        bufferField.setAccessible(true);
        byte[] buffer = (byte[]) bufferField.get(session.mProcessToTerminalIOQueue);
        assertEquals(64 * 1024, buffer.length);
    }

    @Test
    public void testEmulatorTopRowPreserved() {
        withTerminalSized(10, 5);
        assertEquals(0, mTerminal.getTopRow());
        mTerminal.setTopRow(-12);
        assertEquals(-12, mTerminal.getTopRow());
    }

    @Test
    public void testBlockSetClearsLineWrapOnFullRowClear() {
        withTerminalSized(4, 3);
        enterString("abcdef");
        TerminalBuffer screen = mTerminal.getScreen();
        assertTrue(screen.getLineWrap(0));
        screen.blockSet(0, 0, 4, 1, ' ', TextStyle.NORMAL);
        assertFalse(screen.getLineWrap(0));
    }

    @Test
    public void testPrimaryDeviceAttributesAdvertisesSixel() {
        withTerminalSized(10, 5);
        assertEnteringStringGivesResponse("\033[c", "\033[?64;1;2;4;6;9;15;18;21;22c");
    }

    @Test
    public void testOsc52SupportsPayloadsUpTo100Kb() {
        withTerminalSized(10, 5);
        byte[] raw = new byte[16 * 1024];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) ('A' + (i % 26));
        }
        String expected = new String(raw, StandardCharsets.US_ASCII);
        String b64 = Base64.getEncoder().encodeToString(raw);
        enterString("\033]52;c;" + b64 + "\007");
        assertEquals(1, mOutput.clipboardPuts.size());
        assertEquals(expected, mOutput.clipboardPuts.get(0));
    }

    @Test
    public void testITermImageAndSystemPropertiesPresent() {
        assertNotNull(AndroidUtils.getSystemProperties("UpstreamTerminalFeaturesTest"));
        ITermImage img = new ITermImage(null, false);
        StringBuilder args = new StringBuilder("1337;File=inline=1;width=10px;height=10px:AAAA");
        int idx = img.readArguments(mTerminal, args, "1337;File=".length());
        assertTrue(idx >= "1337;File=".length());
        assertTrue(img.isInline());
        assertEquals(10, img.getWidth());
        assertEquals(10, img.getHeight());
        assertTrue(img.readImage(args, idx));
    }
}
