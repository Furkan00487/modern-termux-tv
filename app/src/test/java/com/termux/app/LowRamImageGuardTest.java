package com.termux.app;

import android.graphics.Bitmap;

import com.termux.terminal.ITermImage;
import com.termux.terminal.TerminalBitmap;
import com.termux.terminal.TerminalBuffer;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalOutput;
import com.termux.terminal.TerminalSixel;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class LowRamImageGuardTest {

    private static final long ONE_MIB = 1024L * 1024L;

    private static class NoOpTerminalOutput extends TerminalOutput {
        @Override public void write(byte[] data, int offset, int count) {}
        @Override public void titleChanged(String oldTitle, String newTitle) {}
        @Override public void onCopyTextToClipboard(String text) {}
        @Override public void onPasteTextFromClipboard() {}
        @Override public void onBell() {}
        @Override public void onColorsChanged() {}
    }

    @After
    public void tearDown() {
        TerminalBitmap.resetImageLimits();
    }

    @Test
    public void lowRamPolicyConfiguresAndResetsTerminalImageLimits() {
        LowRamPolicy.applyTerminalImagePolicy(1536L * ONE_MIB);
        assertEquals(LowRamPolicy.LOW_RAM_MAX_BITMAP_BYTES, TerminalBitmap.getEffectiveMaxBitmapSize());
        assertEquals(LowRamPolicy.LOW_RAM_MAX_TOTAL_BITMAP_BYTES, TerminalBitmap.getEffectiveMaxTotalBitmapsSize());
        assertEquals(LowRamPolicy.LOW_RAM_MAX_BITMAP_DIMENSION_PX, TerminalBitmap.getEffectiveMaxBitmapDimension());

        LowRamPolicy.applyTerminalImagePolicy(LowRamPolicy.TWO_GIB_BYTES);
        assertEquals(TerminalBitmap.MAX_BITMAP_SIZE, TerminalBitmap.getEffectiveMaxBitmapSize());
        assertEquals(0, TerminalBitmap.getEffectiveMaxTotalBitmapsSize());
        assertEquals(0, TerminalBitmap.getEffectiveMaxBitmapDimension());
    }

    @Test
    public void oversizedSixelIsRejectedCleanlyUnderLowRamPolicy() {
        LowRamPolicy.applyTerminalImagePolicy(1024L * ONE_MIB);

        // 4096x4096 exceeds both 8 MiB single bitmap cap and 2048 px max dimension
        assertFalse(TerminalBitmap.isBitmapSizeAllowed(4096, 4096));
        assertFalse(TerminalBitmap.isBitmapSizeAllowed(2500, 100));
        assertNull(TerminalSixel.build(null, 4096, 4096));

        // 1920x1080 = 8,294,400 bytes <= 8 MiB and <= 2048 px
        assertTrue(TerminalBitmap.isBitmapSizeAllowed(1920, 1080));

        // Emulator DCS sixel sequence with oversized raster attributes must be ignored cleanly
        TerminalEmulator emulator = new TerminalEmulator(new NoOpTerminalOutput(), 80, 24, 10, 20, 100, null);
        String oversizedSixel = "\033Pq\"1;1;4096;4096#0;2;100;100;100#0!10~-\033\\";
        byte[] bytes = oversizedSixel.getBytes(StandardCharsets.US_ASCII);
        emulator.append(bytes, bytes.length);
        assertEquals(0, emulator.getScreen().getTerminalBitmapCount());
    }

    @Test
    public void smallSixelRendersUnderLowRamPolicy() {
        LowRamPolicy.applyTerminalImagePolicy(1024L * ONE_MIB);

        TerminalEmulator emulator = new TerminalEmulator(new NoOpTerminalOutput(), 80, 24, 10, 20, 100, null);
        String smallSixel = "\033Pq#0;2;100;0;0#0!10~-\033\\";
        byte[] bytes = smallSixel.getBytes(StandardCharsets.US_ASCII);
        emulator.append(bytes, bytes.length);
        assertEquals(1, emulator.getScreen().getTerminalBitmapCount());
    }

    @Test
    public void oversizedITermImagePayloadIsRejectedCleanlyUnderLowRamPolicy() {
        LowRamPolicy.applyTerminalImagePolicy(1024L * ONE_MIB);

        // Exceeds single bitmap byte limit via multipart chunks
        ITermImage img = new ITermImage(null, true);
        StringBuilder initArgs = new StringBuilder("1337;MultipartFile=inline=1");
        int idx = img.readArguments(null, initArgs, "1337;MultipartFile=".length());
        assertTrue(idx >= 0);

        byte[] chunk = new byte[1024 * 1024];
        StringBuilder partArgs = new StringBuilder("1337;FilePart=");
        for (int i = 0; i < chunk.length; i++) {
            partArgs.append('A');
        }

        boolean failedOnOversize = false;
        for (int i = 0; i < 10; i++) {
            if (!img.readImage(partArgs, "1337;FilePart=".length())) {
                failedOnOversize = true;
                break;
            }
        }
        assertTrue("Multipart OSC 1337 exceeding low-RAM cap must be rejected", failedOnOversize);
    }

    @Test
    public void oversizedBitmapDimensionsAreRejectedAndCacheBudgetEvictsOldestUnderLowRamPolicy() {
        LowRamPolicy.applyTerminalImagePolicy(1024L * ONE_MIB);

        Bitmap validBmp = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        assertTrue(validBmp.compress(Bitmap.CompressFormat.PNG, 100, pngOut));
        byte[] validPng = pngOut.toByteArray();

        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);
        // Requesting oversized target dimensions must be rejected cleanly without OOM
        int[] rejectedDelta = buffer.addTerminalBitmapForImage(validPng, 0, 0, 10, 20, 4096, 4096, false);
        assertEquals(0, rejectedDelta[0]);
        assertEquals(0, rejectedDelta[1]);
        assertEquals(0, buffer.getTerminalBitmapCount());

        // Valid small image succeeds
        int[] okDelta = buffer.addTerminalBitmapForImage(validPng, 0, 0, 10, 20, 10, 20, false);
        assertTrue(okDelta[0] > 0);
        assertEquals(1, buffer.getTerminalBitmapCount());

        // Tight total bitmap budget evicts oldest bitmaps when exceeded
        TerminalBitmap.setLowRamImageLimits(8 * 1024 * 1024, 4 * 1024 * 1024, 2048);
        Bitmap largeAllowedBmp = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888); // 3,200,000 bytes (~3.05 MiB)
        assertNotNull(TerminalBitmap.build(buffer, 1001, largeAllowedBmp, 0, 0, 10, 20));
        buffer.addTerminalBitmapForTesting(1001, TerminalBitmap.build(buffer, 1001, largeAllowedBmp, 0, 0, 10, 20));
        // Second ~3.05 MiB bitmap exceeds 4 MiB total budget -> old bitmaps evicted
        Bitmap largeAllowedBmp2 = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888);
        buffer.addTerminalBitmapForTesting(1002, TerminalBitmap.build(buffer, 1002, largeAllowedBmp2, 0, 1, 10, 20));
        assertTrue(buffer.getTotalTerminalBitmapBytes() <= 4 * 1024 * 1024L);
        assertEquals(1, buffer.getTerminalBitmapCount());
    }
}
