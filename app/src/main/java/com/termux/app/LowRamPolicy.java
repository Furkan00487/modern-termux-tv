package com.termux.app;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Policy helper for detecting devices with less than 2 GiB of reported total RAM
 * and formatting memory sizes for the TV low-memory warning dialog.
 */
public final class LowRamPolicy {

    public static final long ONE_MIB_BYTES = 1024L * 1024L;
    public static final long ONE_GIB_BYTES = 1024L * ONE_MIB_BYTES;
    public static final long TWO_GIB_BYTES = 2L * ONE_GIB_BYTES;

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
}
