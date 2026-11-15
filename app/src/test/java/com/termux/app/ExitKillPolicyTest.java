package com.termux.app;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExitKillPolicyTest {

    @Test
    public void defaultsEnabledOnTvAndDisabledOnNonTv() {
        assertTrue(ExitKillPolicy.isAutoKillEnabled(true, null));
        assertFalse(ExitKillPolicy.isAutoKillEnabled(false, null));
    }

    @Test
    public void explicitUserPreferenceOverridesDeviceDefault() {
        assertFalse(ExitKillPolicy.isAutoKillEnabled(true, Boolean.FALSE));
        assertTrue(ExitKillPolicy.isAutoKillEnabled(false, Boolean.TRUE));
    }

    @Test
    public void killsOnExplicitExitsWhenEnabled() {
        assertTrue(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.BACK_PRESSED, 0, false));
        assertTrue(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.LAST_SESSION_FINISHED, 0, false));
        assertTrue(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.TASK_REMOVED, 0, false));
        assertTrue(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.NOTIFICATION_STOP, 0, false));
    }

    @Test
    public void backgroundKillRequiresZeroActiveActivitiesAndNoConfigChange() {
        assertTrue(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.BACKGROUNDED, 0, false));
        assertFalse(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.BACKGROUNDED, 1, false));
        assertFalse(ExitKillPolicy.shouldKillProcess(true, null, ExitKillPolicy.ExitReason.BACKGROUNDED, 0, true));
    }

    @Test
    public void disabledPreferencePreventsAutoKillOnExitAndBackground() {
        assertFalse(ExitKillPolicy.shouldKillProcess(true, Boolean.FALSE, ExitKillPolicy.ExitReason.BACK_PRESSED, 0, false));
        assertFalse(ExitKillPolicy.shouldKillProcess(true, Boolean.FALSE, ExitKillPolicy.ExitReason.BACKGROUNDED, 0, false));
        assertFalse(ExitKillPolicy.shouldKillProcess(false, null, ExitKillPolicy.ExitReason.BACKGROUNDED, 0, false));
    }
}
