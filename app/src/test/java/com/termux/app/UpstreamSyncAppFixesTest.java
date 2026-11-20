package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;

import com.termux.app.activities.SettingsActivity;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class UpstreamSyncAppFixesTest {

    @Test
    public void runCommandServiceRejectsDisallowedExternalAppsBeforeFileResultConfig() {
        File externalDir = Environment.getExternalStorageDirectory();
        File resultDir = new File(externalDir, "run_command_security_test_" + System.nanoTime());
        assertFalse(resultDir.exists());

        Intent intent = new Intent(RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND);
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, "/system/bin/true");
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_RESULT_DIRECTORY, resultDir.getAbsolutePath());
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_RESULT_SINGLE_FILE, true);
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_RESULT_FILE_BASENAME, "pwn.txt");
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_RESULT_FILE_ERROR_FORMAT, "allow-external-apps=true");

        RunCommandService service = Robolectric.buildService(RunCommandService.class)
            .create()
            .get();
        service.onStartCommand(intent, 0, 1);

        File targetFile = new File(resultDir, "pwn.txt");
        assertFalse("Disallowed external app command must not write error result file", targetFile.exists());
    }

    @Test
    public void contentProviderGetTypeHandlesNullLastPathSegment() {
        TermuxOpenReceiver.ContentProvider provider = new TermuxOpenReceiver.ContentProvider();
        assertNull(provider.getType(Uri.parse("content://com.termux.files/")));
        assertEquals("text/plain", provider.getType(Uri.parse("content://com.termux.files/hello.txt")));
    }

    @Test
    public void applicationPreferencesRoutesToSettingsActivity() {
        Context context = RuntimeEnvironment.getApplication();
        Intent prefsIntent = new Intent(Intent.ACTION_APPLICATION_PREFERENCES);
        prefsIntent.setPackage(context.getPackageName());
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(prefsIntent, 0);
        assertNotNull("APPLICATION_PREFERENCES must resolve to an activity", resolveInfo);
        assertEquals(SettingsActivity.class.getName(), resolveInfo.activityInfo.name);
    }

    @Test
    public void tvBuildKeepsX11Excluded() throws Exception {
        File repoRoot = new File("..").getCanonicalFile();
        if (!new File(repoRoot, "settings.gradle").exists()) {
            repoRoot = new File(".").getCanonicalFile();
        }
        File settingsGradle = new File(repoRoot, "settings.gradle");
        assertTrue("settings.gradle must exist at " + settingsGradle, settingsGradle.exists());
        String settingsContent = new String(Files.readAllBytes(settingsGradle.toPath()), StandardCharsets.UTF_8);
        assertFalse("TV repo must not include :x11", settingsContent.contains(":x11"));
        assertFalse("TV repo must not contain x11 directory", new File(repoRoot, "x11").exists());
    }
}
