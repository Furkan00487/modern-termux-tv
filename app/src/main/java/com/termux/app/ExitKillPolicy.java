package com.termux.app;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Policy helper deciding when Modern Termux TV should immediately terminate its
 * process on exit or background transitions (default enabled on Android TVs).
 */
public final class ExitKillPolicy {

    public enum ExitReason {
        BACK_PRESSED,
        LAST_SESSION_FINISHED,
        NOTIFICATION_STOP,
        TASK_REMOVED,
        BACKGROUNDED
    }

    private ExitKillPolicy() {
    }

    public static boolean isAutoKillEnabled(boolean isTv, @Nullable Boolean userOverride) {
        if (userOverride != null) {
            return userOverride;
        }
        return isTv;
    }

    public static boolean shouldKillProcess(boolean isTv, @Nullable Boolean userOverride,
                                            @NonNull ExitReason reason,
                                            int activeStartedActivities,
                                            boolean isChangingConfigurations) {
        if (reason == ExitReason.NOTIFICATION_STOP) {
            return true;
        }
        if (!isAutoKillEnabled(isTv, userOverride)) {
            return false;
        }
        if (reason == ExitReason.BACKGROUNDED) {
            return activeStartedActivities <= 0 && !isChangingConfigurations;
        }
        return true;
    }

    public static boolean isTelevisionDevice(@NonNull Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }
}
