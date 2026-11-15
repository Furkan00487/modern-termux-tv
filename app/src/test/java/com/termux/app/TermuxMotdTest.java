package com.termux.app;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TermuxMotdTest {
    @Test
    public void testModernTermuxMotdContentContainsBrandAndCommands() {
        String motd = TermuxInstaller.getModernTermuxMotdContent();
        assertTrue(motd.contains("MODERN TERMUX'A HOŞ GELDİN!"));
        assertTrue(motd.contains("pkg search <sorgu>"));
        assertTrue(motd.contains("pkg install <paket>"));
        assertTrue(motd.contains("https://github.com/Furkan00487/modern-termux"));
        assertFalse(motd.contains("Welcome to Termux!"));
        assertFalse(motd.contains("pkg install x11-repo"));
    }

    @Test
    public void testEnsureModernTermuxMotdFileUpdatesLegacyMotd() throws Exception {
        File tempMotd = File.createTempFile("test_motd", ".txt");
        tempMotd.deleteOnExit();

        String legacyMotd = "Welcome to Termux!\nDocs: https://termux.dev/docs\nSubscribing to additional repositories:\n - X11: pkg install x11-repo\n";
        java.nio.file.Files.write(tempMotd.toPath(), legacyMotd.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        TermuxInstaller.ensureModernTermuxMotdFile(tempMotd);

        String updated = new String(java.nio.file.Files.readAllBytes(tempMotd.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(updated.contains("MODERN TERMUX'A HOŞ GELDİN!"));
        assertFalse(updated.contains("Welcome to Termux!"));
        assertFalse(updated.contains("pkg install x11-repo"));
    }

    @Test
    public void testEnsureModernTermuxMotdFileCreatesIfMissing() throws Exception {
        File tempDir = File.createTempFile("test_etc", "");
        tempDir.delete();
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        File missingMotd = new File(tempDir, "motd");
        TermuxInstaller.ensureModernTermuxMotdFile(missingMotd);

        assertTrue(missingMotd.exists());
        String content = new String(java.nio.file.Files.readAllBytes(missingMotd.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(content.contains("MODERN TERMUX'A HOŞ GELDİN!"));
    }

    @Test
    public void testNotificationDrawableExists() {
        assertTrue(com.termux.R.drawable.ic_service_notification != 0);
    }

    @Test
    public void testTermuxMotdRefreshScriptContent() {
        String script = TermuxInstaller.getTermuxMotdRefreshScriptContent();
        assertTrue(script.contains("com.termux.motd"));
        assertTrue(script.contains("[HATA]"));
        assertTrue(script.contains("dyn-motd.sh"));
        assertTrue(script.contains("exit 1"));
    }
}
