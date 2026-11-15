# Modern Termux TV

[![Release](https://img.shields.io/github/v/release/Furkan00487/modern-termux-tv?label=release&color=00E5FF)](https://github.com/Furkan00487/modern-termux-tv/releases/latest)
[![Release Build](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/release_build.yml/badge.svg)](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/release_build.yml)
[![Unit tests](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/run_tests.yml/badge.svg)](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/run_tests.yml)
[![License: GPL v3](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)

🌐 **[English](README.md)** | **[Türkçe](README.tr.md)**

---

**Modern Termux TV** is an enhanced Android TV–focused fork of the [Termux](https://github.com/termux/termux-app) terminal emulator and Linux environment. It ships with a **Jetpack Compose for TV** interface, full D-pad remote control navigation, a bundled **`mksh`** default shell, low-RAM (`< 2 GiB`) protection and memory-safe TV exit cleanup, out-of-the-box audio and microphone support, and split **`arm64-v8a`** / **`armeabi-v7a`** signed release builds.

> This project is a fork of [termux/termux-app](https://github.com/termux/termux-app). The terminal emulation engine, Linux shell environment, and package repository ecosystem originate from the upstream Termux project and its contributors.

---

## Contents

- [Why Modern Termux TV?](#why-modern-termux-tv)
- [Key Features](#key-features)
- [TV Remote & D-Pad Controls](#tv-remote--d-pad-controls)
- [Audio & Microphone Setup](#audio--microphone-setup)
- [Built-in CLI Utilities](#built-in-cli-utilities)
- [Installation](#installation)
- [Verifying a Download](#verifying-a-download)
- [Upgrading and Uninstalling](#upgrading-and-uninstalling)
- [Building from Source](#building-from-source)
- [Debugging](#debugging)
- [Contributing](#contributing)
- [Upstream Resources](#upstream-resources)
- [License](#license)

---

## Why Modern Termux TV?

| Capability / Feature | Upstream Termux | Modern Termux TV |
| :--- | :--- | :--- |
| **Android TV UI** | Legacy phone Views & `ListView` drawer | **Leanback TV support + Jetpack Compose for TV** (`androidx.tv:tv-material`) |
| **Remote Navigation** | No dedicated TV action bar or scroll mode | **Top TV Action Bar, Scroll Mode, D-pad Extra Keys, focus-trapped Drawer** |
| **Low-RAM TV Protection** | Stays resident in memory by default | **< 2 GiB RAM warning overlay** + **TV exit/background auto-kill policy** |
| **Default Shell** | `bash` | **Bundled `mksh` (`mksh_59c-5`)** automatically linked as `~/.termux/shell` |
| **TV Architectures** | Generic distribution | **Split signed `arm64-v8a` (64-bit) & `armeabi-v7a` (32-bit TVs)** |
| **Microphone & Audio** | Manual permission debugging, external script hunting | **Native `RECORD_AUDIO` permission** + `termux-setup-microphone` |
| **UI Theme & Accent** | Default dark grey/black scheme | **Dark Teal (`#80CBC4` / `#0E1113`) Compose TV UI** + **Cyber Midnight (`#00E5FF`) cursor & notification** |
| **Release Signing** | Often distributed as unsigned or public test keys | **Signed in GitHub Actions** with dedicated private release key (v2 & v3) |
| **Welcome Banner (MOTD)** | English default | **Modern Turkish MOTD** synchronized on app upgrade + `termux-motd-refresh` |

---

## Key Features

- **Jetpack Compose for TV UI:** Replaces legacy XML drawer, settings, and dialog views on TV with Compose Material 3 screens (`TerminalDrawerPanel`, `TvTerminalActionBar`, `TvSettingsScreen`, and `LowRamWarningOverlay`).
- **Leanback & D-Pad First:** Declares `LEANBACK_LAUNCHER` with a TV banner and optional touchscreen hardware (`required="false"`), enabling full terminal control from a standard TV remote.
- **Memory-Safe TV Lifecycle:** Shows a dismissible warning overlay on devices with `< 2 GiB` total RAM and automatically terminates the Termux process on TV exit/background (`ExitKillPolicy`, configurable in **Termux Debugging** settings).
- **Bundled `mksh` Default Shell:** Fetches, verifies, and packs `mksh_59c-5` into bootstrap archives and symlinks `~/.termux/shell` -> `$PREFIX/bin/mksh`.
- **Audio and microphone ready:** Includes the Android `RECORD_AUDIO` permission out of the box and bundles `termux-setup-microphone` to request runtime audio permissions instantly.
- **Signed split-ABI releases:** Release workflows produce signed `arm64-v8a` and `armeabi-v7a` APKs verified with APK Signature Scheme v2 and v3.

---

## TV Remote & D-Pad Controls

Navigate Modern Termux TV smoothly with a standard D-pad remote:

- **Short press `OK` (`DPAD_CENTER`):** Opens or reopens the on-screen soft keyboard in normal terminal mode (or sends Enter `\r` in alternate-screen apps or when the keyboard is already visible).
- **Long-press `OK` or press `MENU`:** Focuses the **Top TV Action Bar** (`Sessions`, `Keyboard`, `Extra Keys`, `Scroll`, `Scroll Up/Down`, `Menu`, `Settings`). Press `Back`, `Escape`, or `Down` to return focus to the terminal.
- **Top Bar -> `Extra Keys`:** Moves D-pad focus into the bottom Extra Keys bar (`ESC`, `TAB`, `HOME`, `CTRL`, `ALT`, arrows, `PGUP`/`PGDN`) with visible focus highlights; press `Up` from the top row or `Back` to return to the terminal.
- **Scroll Mode (`Scroll` button or `CH+` / `CH-`):** Scrolls terminal history up/down; in Scroll Mode, `Left` opens the Sessions drawer and `OK` / `Back` exits Scroll Mode.
- **Sessions Drawer (`Sessions`):** Focus-trapped Compose drawer to switch sessions, rename/close sessions, open Named/Failsafe sessions, toggle keyboard/toolbar, or open Settings (`⚙`).

---

## Audio & Microphone Setup

Modern Termux includes native Android microphone support (`RECORD_AUDIO`). You can configure full duplex audio (playback and recording) using PulseAudio and ALSA.

### 1. Grant Microphone Permission

Run the bundled microphone helper script:

```bash
termux-setup-microphone
```

A system dialog will prompt you to grant microphone access to Modern Termux. Tap **Allow** (While using the app).

### 2. Install Audio Packages

```bash
pkg install pulseaudio alsa-utils
```

### 3. Start PulseAudio with OpenSL ES Support

To enable audio playback and recording in terminal CLI apps, start PulseAudio with OpenSL ES sink and source modules:

```bash
pulseaudio --start \
    --load="module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" \
    --load="module-sles-sink" \
    --load="module-sles-source" \
    --exit-idle-time=-1
```

> [!TIP]
> You can add this command to your `~/.mkshrc` or `~/.profile` to initialize sound automatically whenever you start a session.

### 4. Test Playback and Recording

- **Test Microphone Recording (ALSA):**
  Record 5 seconds of audio to `test.wav`:
  ```bash
  arecord -D pulse -f cd -d 5 test.wav
  ```
- **Test Audio Playback (ALSA):**
  Play back the recorded audio:
  ```bash
  aplay -D pulse test.wav
  ```
- **PulseAudio Native Tools:**
  You can also record and play using `parec` and `paplay`:
  ```bash
  parec --format=s16le --rate=44100 --channels=2 > mic_sample.raw
  paplay --format=s16le --rate=44100 --channels=2 mic_sample.raw
  ```

---

## Built-in CLI Utilities

Modern Termux TV installs helper commands directly into `$PREFIX/bin`:

### `termux-setup-microphone`
Broadcasts the intent request to prompt the Android system for runtime audio recording permission (`android.permission.RECORD_AUDIO`).

### `termux-motd-refresh`
Refreshes and displays the dynamic MOTD banner with live Battery, RAM, and Storage stats if the **Modern Termux:MOTD** (`com.termux.motd`) companion app is installed. If the companion app is not installed, it outputs a clean, colored error guiding you to install it.

### `mksh`
MirBSD Korn Shell (`mksh_59c-5`) bundled inside the bootstrap and configured as the default `~/.termux/shell`.

---

## Installation

Modern Termux TV requires **Android 7.0 or later (`minSdk 24`)** and ships split APKs for **`arm64-v8a`** (64-bit) and **`armeabi-v7a`** (32-bit, widely used on Android TV devices).

1. Navigate to the [latest release](https://github.com/Furkan00487/modern-termux-tv/releases/latest).
2. Download the APK matching your device ABI (check via `adb shell getprop ro.product.cpu.abi`: `..._arm64-v8a.apk` or `..._armeabi-v7a.apk`) and its corresponding `..._sha256sums` file.
3. [Verify the download integrity](#verifying-a-download), then install the APK on your device.

### Compatibility with Other Termux Builds

Modern Termux is signed with its own dedicated private release key. Android package management prevents installing this build over existing Termux installations signed by F-Droid, upstream GitHub releases, or Google Play (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

To switch to Modern Termux:
1. [Back up your existing Termux data](https://wiki.termux.com/wiki/Backing_up_Termux) if necessary.
2. Uninstall your current Termux app and any installed Termux plugins.
3. Install Modern Termux APK.

---

## Verifying a Download

Check the SHA-256 checksum from the directory containing both files:

```bash
sha256sum --ignore-missing -c termux-app_<version>-apt-android-7-github-release_sha256sums
```

Verify the APK signature with `apksigner` (from the Android SDK build tools or the Termux `apksigner` package):

```bash
apksigner verify --verbose termux-app_<version>-apt-android-7-github-release_<abi>.apk
```

The output will confirm validity under both **Scheme v2** and **Scheme v3**:
```
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
```

---

## Upgrading and Uninstalling

- **Upgrading:** Installing a newer release over an existing Modern Termux TV installation upgrades the app in place and preserves all user files and packages. When `versionCode` increases, Modern Termux TV automatically refreshes helper scripts (`termux-setup-microphone`, `termux-motd-refresh`), updates the MOTD banner, and checks the default `mksh` shell symlink.
- **Uninstalling:** Remove Modern Termux from **Android Settings** -> **Apps** -> **Modern Termux** -> **Uninstall**.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Furkan00487/modern-termux-tv.git
cd modern-termux-tv

# Run unit tests across all modules
./gradlew test

# Download bootstraps + mksh and build split release APKs
./gradlew downloadBootstraps assembleRelease
```

Release signing in CI uses the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, and `KEYSTORE_ALIAS` secrets. In local environments without these variables, `assembleRelease` produces unsigned APKs.

---

## Debugging

Modern Termux TV provides English and localized Turkish UI strings (following the Android system language). To capture diagnostic logs for troubleshooting:

1. **Open Settings:**
   - Long-press `OK` (or press `MENU`) to focus the Top TV Action Bar and select **Settings (`⚙`)**, or
   - Open the **Sessions Drawer** and select the gear icon (**Settings `⚙`**).
2. **Access Log Level:**
   - Select **Termux Debugging** (**Termux Hata Ayıklama**) in the left category rail.
   - Open **Log Level** (**Günlük Düzeyi**).
3. **Select Verbosity:**
   - Select **Verbose** (**Ayrıntılı**) or **Debug** (**Hata Ayıklama**).
   - Always revert back to **Normal** after testing to avoid performance overhead.

View logs from within Termux:
```bash
logcat -d | grep -i "com.termux"
```

Or from a connected PC via ADB:
```bash
adb logcat -s "termux:*"
```

---

## Contributing

- Keep pull requests focused and run `./gradlew test` prior to opening a PR.
- Follow [Conventional Commits](https://www.conventionalcommits.org) (e.g. `feat: add dynamic notification icon`, `fix: handle null service instance`).
- When releasing a new version, bump `versionCode` (+1 integer) and `versionName` (SemVer `major.minor.patch`) in `app/build.gradle` and submodule gradle files.
- Place shared utilities in `termux-shared` to maintain modular separation.

---

## Upstream Resources

- [Termux Wiki](https://wiki.termux.com/wiki/)
- [Termux Packages Repository](https://github.com/termux/termux-packages)
- [Termux File System Layout](https://github.com/termux/termux-packages/wiki/Termux-file-system-layout)
- [Running Commands via RUN_COMMAND Intent](https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent)

---

## License

Modern Termux is licensed under the [GNU General Public License v3.0](LICENSE).
- Upstream terminal emulation libraries from [Terminal Emulator for Android](https://github.com/jackpal/Android-Terminal-Emulator) are licensed under Apache 2.0. See `terminal-view` and `terminal-emulator`.
- Additional notices can be found in `termux-shared/LICENSE.md`.
