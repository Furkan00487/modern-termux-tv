package com.termux.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LowRamPolicyTest {

    private static final long ONE_MIB = 1024L * 1024L;

    @Test
    public void underTwoGiBIsDetectedAsLowRam() {
        assertTrue(LowRamPolicy.isLowRam(1024L * ONE_MIB));
        assertTrue(LowRamPolicy.isLowRam(1850L * ONE_MIB));
        assertTrue(LowRamPolicy.isLowRam(LowRamPolicy.TWO_GIB_BYTES - 1L));
    }

    @Test
    public void twoGiBOrMoreIsNotLowRam() {
        assertFalse(LowRamPolicy.isLowRam(LowRamPolicy.TWO_GIB_BYTES));
        assertFalse(LowRamPolicy.isLowRam(3072L * ONE_MIB));
        assertFalse(LowRamPolicy.isLowRam(4096L * ONE_MIB));
    }

    @Test
    public void nonPositiveMemoryDoesNotTriggerWarning() {
        assertFalse(LowRamPolicy.isLowRam(0L));
        assertFalse(LowRamPolicy.isLowRam(-1024L));
        assertFalse(LowRamPolicy.shouldShowWarning(0L, false));
    }

    @Test
    public void dismissedWarningIsSuppressed() {
        long oneAndHalfGiB = 1536L * ONE_MIB;
        assertTrue(LowRamPolicy.shouldShowWarning(oneAndHalfGiB, false));
        assertFalse(LowRamPolicy.shouldShowWarning(oneAndHalfGiB, true));
    }

    @Test
    public void formatMemorySizeFormatsMiBAndGiB() {
        assertEquals("950 MiB", LowRamPolicy.formatMemorySize(950L * ONE_MIB));
        assertEquals("1.8 GiB", LowRamPolicy.formatMemorySize(1843L * ONE_MIB));
        assertEquals("2.0 GiB", LowRamPolicy.formatMemorySize(LowRamPolicy.TWO_GIB_BYTES));
    }
}
