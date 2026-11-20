package com.termux.app;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;

import com.termux.terminal.TerminalBitmap;

import java.util.Locale;

/**
 * Policy helper for detecting devices with less than 2 GiB of reported total RAM
 * and formatting memory sizes for the TV low-memory warning dialog.
 */
public final class LowRamPolicy {

    public static final long ONE_MIB_BYTES = 1024L * 1024L;
    public static final long ONE_GIB_BYTES = 1024L * ONE_MIB_BYTES;
    public static final long TWO_GIB_BYTES = 2L * ONE_GIB_BYTES;

    public static final int LOW_RAM_MAX_BITMAP_BYTES = 8 * 1024 * 1024;
    public static final int LOW_RAM_MAX_TOTAL_BITMAP_BYTES = 16 * 1024 * 1024;
    public static final int LOW_RAM_MAX_BITMAP_DIMENSION_PX = 2048;

    private LowRamPolicy() {
    }

    public static boolean isLowRam(long totalMemBytes) {
        return totalMemBytes > 0L && totalMemBytes < TWO_GIB_BYTES;
    }

    public static boolean shouldShowWarning(long totalMemBytes, boolean dismissed) {
        return !dismissed && isLowRam(totalMemBytes);
    }

    public static String formatMemorySize(long totalMemBytes) {
        if (totalMemBytes <= 0L) {
            return "0 MiB";
        }
        if (totalMemBytes < ONE_GIB_BYTES) {
            long mib = Math.round((double) totalMemBytes / ONE_MIB_BYTES);
            return String.format(Locale.US, "%d MiB", mib);
        }
        double gib = (double) totalMemBytes / ONE_GIB_BYTES;
        return String.format(Locale.US, "%.1f GiB", gib);
    }

    public static long getTotalDeviceMemoryBytes(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return -1L;
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    /**
     * Configures {@link TerminalBitmap} memory and dimension limits for low-RAM (&lt; 2 GiB) devices
     * so oversized Sixel / OSC 1337 image payloads are rejected cleanly without OOM.
     */
    public static void applyTerminalImagePolicy(long totalMemBytes) {
        if (isLowRam(totalMemBytes)) {
            TerminalBitmap.setLowRamImageLimits(
                LOW_RAM_MAX_BITMAP_BYTES,
                LOW_RAM_MAX_TOTAL_BITMAP_BYTES,
                LOW_RAM_MAX_BITMAP_DIMENSION_PX);
        } else {
            TerminalBitmap.resetImageLimits();
        }
    }

    public static void applyTerminalImagePolicy(@NonNull Context context) {
        applyTerminalImagePolicy(getTotalDeviceMemoryBytes(context));
    }
}
