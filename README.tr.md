# Modern Termux TV

[![Sürüm](https://img.shields.io/github/v/release/Furkan00487/modern-termux-tv?label=s%C3%BCr%C3%BCm&color=00E5FF)](https://github.com/Furkan00487/modern-termux-tv/releases/latest)
[![Release Derleme](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/release_build.yml/badge.svg)](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/release_build.yml)
[![Birim Testleri](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/run_tests.yml/badge.svg)](https://github.com/Furkan00487/modern-termux-tv/actions/workflows/run_tests.yml)
[![Lisans: GPL v3](https://img.shields.io/badge/lisans-GPL--3.0-blue.svg)](LICENSE)

🌐 **[English](README.md)** | **[Türkçe](README.tr.md)**

---

**Modern Termux TV**, [Termux](https://github.com/termux/termux-app) Android terminal emülatörünün ve Linux ortamının **Android TV** odaklı gelişmiş bir çatalıdır (fork). **Jetpack Compose for TV** arayüzü, eksiksiz kumanda (D-Pad) gezinme desteği, varsayılan **`mksh`** kabuğu, düşük RAM (`< 2 GiB`) uyarısı ve TV bellek koruması, kullanıma hazır ses/mikrofon altyapısı ve çift mimarili (**`arm64-v8a`** / **`armeabi-v7a`**) resmi imzalı kararlı sürüm paketleriyle birlikte gelir.

> Bu proje, [termux/termux-app](https://github.com/termux/termux-app) projesinin bir çatalıdır. Terminal emülasyon motoru, Linux kabuk ortamı ve paket ekosistemi orijinal upstream Termux projesine ve katkıda bulunanlarına aittir.

---

## İçindekiler

- [Neden Modern Termux TV?](#neden-modern-termux-tv)
- [Temel Özellikler](#temel-özellikler)
- [TV Kumandası ve D-Pad Kontrolleri](#tv-kumandası-ve-d-pad-kontrolleri)
- [Ses ve Mikrofon Yapılandırması](#ses-ve-mikrofon-yapılandırması)
- [Dahili CLI Komutları](#dahili-cli-komutları)
- [Kurulum](#kurulum)
- [İndirilen Dosyayı Doğrulama](#indirilen-dosyayı-doğrulama)
- [Sürüm Yükseltme ve Kaldırma](#sürüm-yükseltme-ve-kaldırma)
- [Kaynaktan Derleme](#kaynaktan-derleme)
- [Hata Ayıklama (Debugging)](#hata-ayıklama-debugging)
- [Katkıda Bulunma](#katkıda-bulunma)
- [Bağlantılar ve Kaynaklar](#bağlantılar-ve-kaynaklar)
- [Lisans](#lisans)

---

## Neden Modern Termux TV?

| Yetenek / Özellik | Standart Termux | Modern Termux TV |
| :--- | :--- | :--- |
| **Android TV Arayüzü** | Eski telefon View ve `ListView` çekmecesi | **Leanback TV desteği + Jetpack Compose for TV** (`androidx.tv:tv-material`) |
| **Kumanda (D-Pad) Gezinme** | TV kontrol çubuğu veya kaydırma modu yok | **Üst TV Kontrol Çubuğu, Kaydırma Modu, D-Pad Alt Tuşlar, odak kilitli Çekmece** |
| **Düşük RAM TV Koruması** | Çıkış/arka plan sonrası bellekte kalır | **< 2 GiB RAM uyarı katmanı** + **TV çıkış/arka plan otomatik sonlandırma** |
| **Varsayılan Kabuk** | `bash` | **Dahili `mksh` (`mksh_59c-5`)**, otomatik `~/.termux/shell` bağlantısıyla |
| **TV Mimarileri** | Genel paketleme | **Ayrı imzalı `arm64-v8a` (64-bit) ve `armeabi-v7a` (32-bit TV'ler)** |
| **Mikrofon ve Ses** | İzin sorunları, harici script arayışı | **Doğrudan `RECORD_AUDIO` izni** + `termux-setup-microphone` scripti |
| **Görsel Tema ve Vurgu** | Standart koyu gri/siyah tema | **Koyu Teal (`#80CBC4` / `#0E1113`) Compose TV UI** + **Cyber Midnight (`#00E5FF`) imleç ve bildirim** |
| **Paket İmzası** | Genellikle imzasız veya genel test anahtarlarıyla sunulur | **GitHub Actions üzerinde** özel Release Keystore ile çift imzalı (v2 & v3) |
| **Açılış Mesajı (MOTD)** | Standart İngilizce metin | **Modern Türkçe MOTD**, her sürüm yükseltmesinde otomatik eşitlenir + `termux-motd-refresh` |

---

## Temel Özellikler

- **Jetpack Compose for TV Arayüzü:** TV üzerindeki eski XML çekmece, ayar ve diyalog görünümlerinin yerini Compose Material 3 bileşenleri (`TerminalDrawerPanel`, `TvTerminalActionBar`, `TvSettingsScreen` ve `LowRamWarningOverlay`) alır.
- **Leanback ve Kumanda Odaklılık:** `LEANBACK_LAUNCHER`, TV afişi ve isteğe bağlı dokunmatik ekran desteğiyle (`required="false"`) uygulamanın baştan sona TV kumandasıyla kullanılmasını sağlar.
- **Bellek Dostu TV Yaşam Döngüsü:** Toplam RAM'i `2 GiB` altında olan cihazlarda kapatılabilir uyarı diyaloğu gösterir; TV cihazlarında çıkışta veya arka plana geçişte işlemi otomatik sonlandırır (`ExitKillPolicy`, **Termux Hata Ayıklama** ayarlarından yönetilebilir).
- **Dahili `mksh` Varsayılan Kabuğu:** `mksh_59c-5` ikili dosyasını doğrulanmış biçimde bootstrap içerisine paketler ve `~/.termux/shell` -> `$PREFIX/bin/mksh` olarak bağlar.
- **Kullanıma Hazır Ses ve Mikrofon:** Uygulama `RECORD_AUDIO` iznine sahiptir; ses kaydı ve çalma işlemleri için `termux-setup-microphone` komutu dahili olarak sunulur.
- **İmzalı Çift Mimarili Sürümler:** Her sürüm GitHub Actions üzerinde hem `arm64-v8a` hem de `armeabi-v7a` mimarilerine özel derlenir ve v2/v3 şemalarıyla imzalanıp doğrulanır.

---

## TV Kumandası ve D-Pad Kontrolleri

Modern Termux TV'yi yalnızca TV kumandasıyla rahatça yönetebilirsiniz:

- **Kısa `OK` (`DPAD_CENTER`):** Normal terminal ekranında sanal klavyeyi açar/geri getirir (alternatif ekran uygulamalarında veya klavye zaten açıksa `Enter` `\r` gönderir).
- **Uzun `OK` veya `MENU`:** Odağı **Üst TV Kontrol Çubuğu**na taşır (`Oturumlar`, `Klavye`, `Alt Tuşlar`, `Kaydır`, `Yukarı/Aşağı Kaydır`, `Menü`, `Ayarlar`). `Geri`, `ESC` veya `Aşağı` tuşu odağı terminale geri döndürür.
- **Üst Çubuk -> `Alt Tuşlar`:** Odağı alt ekstra tuşlar çubuğuna (`ESC`, `TAB`, `HOME`, `CTRL`, `ALT`, oklar, `PGUP`/`PGDN`) belirgin odak vurgusuyla taşır; üst satırdan `Yukarı` veya `Geri` ile terminale dönersiniz.
- **Kaydırma Modu (`Kaydır` veya `CH+` / `CH-`):** Terminal geçmişini yukarı/aşağı kaydırır; Kaydırma Modunda `Sol` ok Oturumlar çekmecesini açar, `OK` / `Geri` moddan çıkar.
- **Oturumlar Çekmecesi (`Oturumlar`):** Odağı içeride kilitleyen Compose çekmecesiyle oturum değiştirme, adlandırma/kapatma, Adlı/Güvenli oturum açma, klavye/araç çubuğu ve Ayarlar (`⚙`) yönetimi yapılabilir.

---

## Ses ve Mikrofon Yapılandırması

Modern Termux TV yerleşik mikrofon iznine (`RECORD_AUDIO`) sahiptir. PulseAudio ve ALSA kullanarak terminal uygulamaları için çift yönlü (oynatma ve kayıt) ses altyapısı kurabilirsiniz.

### 1. Mikrofon İznini Verin

Dahili mikrofon yetkilendirme komutunu çalıştırın:

```bash
termux-setup-microphone
```

Açılan sistem iletişim kutusunda **İzin Ver** (Uygulamayı kullanırken) seçeneğini işaretleyin.

### 2. Ses Paketlerini Kurun

```bash
pkg install pulseaudio alsa-utils
```

### 3. PulseAudio Sunucusunu OpenSL ES ile Başlatın

Android donanımına doğrudan erişmek ve TCP köprüsü kurmak için PulseAudio'yu şu komutla çalıştırın:

```bash
pulseaudio --start \
    --load="module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" \
    --load="module-sles-sink" \
    --load="module-sles-source" \
    --exit-idle-time=-1
```

> [!TIP]
> Bu komutu `~/.mkshrc` veya `~/.profile` dosyanıza ekleyerek her oturum açılışında ses sunucusunun otomatik devreye girmesini sağlayabilirsiniz.

### 4. Ses Çalma ve Kayıt Testi

- **Mikrofon Kayıt Testi (ALSA):**
  5 saniyelik ses kaydı alın:
  ```bash
  arecord -D pulse -f cd -d 5 deneme.wav
  ```
- **Kayıt Oynatma Testi (ALSA):**
  Kaydettiğiniz sesi dinleyin:
  ```bash
  aplay -D pulse deneme.wav
  ```
- **PulseAudio Doğrudan Araçları:**
  `parec` ve `paplay` ile de test edebilirsiniz:
  ```bash
  parec --format=s16le --rate=44100 --channels=2 > ses_kaydi.raw
  paplay --format=s16le --rate=44100 --channels=2 ses_kaydi.raw
  ```

---

## Dahili CLI Komutları

Modern Termux TV, `$PREFIX/bin` içerisine otomatik olarak şu araçları yerleştirir:

### `termux-setup-microphone`
Android sistemine yayın (broadcast) göndererek mikrofon izni isteme iletişim kutusunu tetikler (`android.permission.RECORD_AUDIO`).

### `termux-motd-refresh`
**Modern Termux:MOTD** (`com.termux.motd`) eklentisi kuruluysa dinamik karşılama ekranını yeniler ve basar. Eklenti kurulu değilse kırmızı renkli açıklayıcı bir uyarı verir.

### `mksh`
Bootstrap içerisine gömülü MirBSD Korn Shell (`mksh_59c-5`) kabuğu; varsayılan olarak `~/.termux/shell` bağlantısı buna ayarlanır.

---

## Kurulum

Modern Termux TV, **Android 7.0 veya daha yeni (`minSdk 24`)** bir sürüm gerektirir ve hem **`arm64-v8a`** (64-bit) hem de **`armeabi-v7a`** (32-bit, Android TV cihazlarda yaygın) mimarileri için split APK sunar.

1. [En Son Sürümler (Releases)](https://github.com/Furkan00487/modern-termux-tv/releases/latest) sayfasını açın.
2. Cihazınıza uygun APK dosyasını (emin değilseniz `adb shell getprop ro.product.cpu.abi`: `..._arm64-v8a.apk` veya `..._armeabi-v7a.apk`) ve ilgili `..._sha256sums` dosyasını indirin.
3. [İndirmeyi doğrulayın](#indirilen-dosyayı-doğrulama) ve APK dosyasını kurun.

### Diğer Termux Sürümleriyle Uyumluluk

Modern Termux kendine özel bir imza anahtarıyla imzalanmıştır. Android güvenlik politikası gereği F-Droid, Google Play veya resmi GitHub sürümleri üzerine doğrudan güncelleme yapılamaz (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

Geçiş yapmak için:
1. Gerekirse mevcut Termux verilerinizi [yedekleyin](https://wiki.termux.com/wiki/Backing_up_Termux).
2. Kurulu Termux uygulamasını ve varsa eklentilerini (Termux:API vb.) kaldırın.
3. Modern Termux APK'sını yükleyin.

---

## İndirilen Dosyayı Doğrulama

Dosyaların bulunduğu dizinde SHA-256 sağlama toplamını kontrol edin:

```bash
sha256sum --ignore-missing -c termux-app_<surum>-apt-android-7-github-release_sha256sums
```

APK imzasını `apksigner` ile kontrol edin:

```bash
apksigner verify --verbose termux-app_<surum>-apt-android-7-github-release_<abi>.apk
```

Çıktıda hem **v2** hem de **v3** imza şemalarının geçerli (`true`) olduğu görülmelidir:

```
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
```

---

## Sürüm Yükseltme ve Kaldırma

- **Sürüm Yükseltme:** Yeni bir Modern Termux TV APK'sını eskisinin üzerine kurduğunuzda verileriniz ve kurulu paketleriniz korunur. `versionCode` arttığında uygulama otomatik olarak "Başlangıç paketleri güncelleniyor…" diyaloğunu açar; `termux-setup-microphone`, `termux-motd-refresh`, Türkçe MOTD ve varsayılan `mksh` kabuğu kurulumunu günceller.
- **Kaldırma:** Cihazınızda **Ayarlar** -> **Uygulamalar** -> **Modern Termux** yolunu izleyerek uygulamayı kaldırabilirsiniz.

---

## Kaynaktan Derleme

```bash
# Depoyu klonlayın
git clone https://github.com/Furkan00487/modern-termux-tv.git
cd modern-termux-tv

# Tüm modüllerde birim testlerini çalıştırın
./gradlew test

# Bootstrap paketleri + mksh ile sürüm APK'larını derleyin
./gradlew downloadBootstraps assembleRelease
```

CI üzerindeki sürüm imzalama adımı `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD` ve `KEYSTORE_ALIAS` secret'larını kullanır. Yerel derlemede bu anahtarlar bulunmadığında `assembleRelease` imzasız APK üretir; bu APK'yı cihazınıza yüklemeden önce kendi anahtarınızla imzalamanız gerekir.

---

## Hata Ayıklama (Debugging)

Modern Termux TV, sistem diline göre Türkçe ve İngilizce arayüz desteği sunar. Sorun giderme günlüklerini toplamak için:

1. **Ayarları Açın:**
   - Kumandada uzun `OK` (veya `MENU`) ile Üst TV Kontrol Çubuğuna geçip **Ayarlar (`⚙`)** simgesini seçin, ya da
   - **Oturumlar** çekmecesindeki dişli simgesine (`⚙`) dokunun.
2. **Günlük Düzeyine Gidin:**
   - Sol kategori listesinden **Termux Hata Ayıklama** (**Termux Debugging**) seçeneğine geçin.
   - **Günlük Düzeyi** (**Log Level**) ayarını açın.
3. **Düzeyi Değiştirin:**
   - **Ayrıntılı (Verbose)** veya **Hata Ayıklama (Debug)** seçin.
   - İşlem bittikten sonra aşırı log yazımını ve pil tüketimini önlemek için yeniden **Normal** düzeyine getirin.

Günlükleri doğrudan Termux içerisinden incelemek için:
```bash
logcat -d | grep -i "com.termux"
```

Veya bağlı bir bilgisayardan ADB üzerinden:
```bash
adb logcat -s "termux:*"
```

---

## Katkıda Bulunma

- Değişikliklerinizi odaklı tutun ve PR açmadan önce `./gradlew test` komutunu çalıştırın.
- Commit mesajlarında [Conventional Commits](https://www.conventionalcommits.org) standardını kullanın (örn. `feat: dinamik bildirim ikonu eklendi`).
- Yeni bir sürüm hazırlarken `app/build.gradle` ve alt modül gradle dosyalarındaki `versionCode` (+1 artış) ve `versionName` (SemVer `major.minor.patch`) değerlerini güncelleyin.
- Modüler mimariyi korumak adına ortak yardımcı sınıfları `termux-shared` kütüphanesi içerisine yerleştirin.

---

## Bağlantılar ve Kaynaklar

- [Termux Wiki](https://wiki.termux.com/wiki/)
- [Termux Paket Deposu](https://github.com/termux/termux-packages)
- [Termux Dosya Sistemi Mimarisi](https://github.com/termux/termux-packages/wiki/Termux-file-system-layout)
- [RUN_COMMAND Intent ile Komut Çalıştırma](https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent)

---

## Lisans

Modern Termux, [GNU General Public License v3.0](LICENSE) altında lisanslanmıştır.
- [Terminal Emulator for Android](https://github.com/jackpal/Android-Terminal-Emulator) projesinden alınan terminal emülasyon kodları Apache 2.0 lisansına tabidir (`terminal-view` ve `terminal-emulator`).
- Diğer lisans detayları için `termux-shared/LICENSE.md` dosyasını inceleyebilirsiniz.
