# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** ist ein Fork von WoW Forever for Android, der speziell für den **privaten Server Classic Forever** entwickelt wurde. Er startet **World of Warcraft: Forever** auf Snapdragon-Handhelds wie eine normale Android-App: Symbol antippen, Play drücken, Spiel läuft.

Verwendet wird Blizzards eigener **Windows-ARM64-Client**, das Spiel läuft also nativ auf der CPU. Wine übersetzt Windows-Aufrufe, DXVK und ein angepasster Turnip-Treiber rendern auf der Adreno-GPU. Nichts wird in x86 emuliert.

> Inoffizielles Community-Projekt. Nicht mit Blizzard verbunden oder von ihr unterstützt. Dieses Repo und die Releases enthalten **keine** Blizzard-Spieldateien.

> 💬 [Classic Forever auf Discord](https://discord.gg/zBmq68jby) · 🌐 [Webseite](http://classicforever.duckdns.org/)

---

## Was dieser Fork hinzufügt

- **Eingebauter Private-Realm-Patcher.** Kotlin-Port von `classic-forever-linux.py`: setzt deinen Auth-Host per `-config BetaSuspendedTest.wtf` und ersetzt den öffentlichen Schlüssel im Speicher. Host und Schlüssel werden in `classic-forever.properties` ohne Neukompilierung konfiguriert.
- **Sprachauswahl.** Englisch, Russisch, Spanisch, Französisch, Deutsch, Portugiesisch (BR), vereinfachtes Chinesisch, Japanisch, Koreanisch und „Systemstandard".
- **Logs auf dem Gerät.** `logs/classic-forever.log` (Patcher) und `logs/app.log` (App) im Spielordner neben `Data/`, Rotation bei 2 MB, ohne adb lesbar.
- **Community-Buttons.** Direkte Links zur Website und zu Discord im Launcher.
- **Praktische Oberfläche.** Gruppierte Einstellungen, App-Version in der Kopfzeile, getrennte Datei- und Kontoaktionen.
- **Native Auflösung standardmäßig.** Neue Installationen starten mit der echten Bildschirmauflösung und `RenderScale` 50 %. Diese Werte werden **nur beim ersten Start** geschrieben; spätere Änderungen bleiben erhalten.
- **Schwarz-weißes Neon-Design.**

---

## Unterstützte Geräte

AYN Thor und Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) getestet; AYN Odin 2, OnePlus 15r, AYN Odin Portal, RedMagic 11 Pro, Poco F7 und Samsung Galaxy S23/S24/S25 werden ebenfalls unterstützt. Voraussetzungen: Snapdragon 8 (Adreno 7xx/8xx), Android 10+ (64 Bit), ca. **60 GB** frei für Spieldaten und 4 GB intern, Wi-Fi.

---

## Installation

1. `Classic-Forever.apk` aus dem [neuesten Release](https://github.com/legustoss/classic-forever-android/releases/latest) installieren. Paketname `app.wowforever`. Bei einem Update von einer älteren WoW-Forever-Build denselben Signaturschlüssel verwenden; anderenfalls zuerst deinstallieren (Spieldaten bleiben erhalten).
2. **Classic Forever** öffnen, **Zugriff auf alle Dateien** erlauben und **Download & Install** antippen. Standardpfad: `/storage/emulated/0/WoW Forever/` (mit **Change Install Location** änderbar). Der ARM64-Client und die CASC-Indizes (~250 MB) werden mit Hashprüfung geladen; der Rest kommt beim Spielen hinzu (bis ~50 GB).
3. Optional vom PC kopieren: `.build.info` (versteckte Datei aus dem Blizzard-Installationsverzeichnis) und den gesamten Ordner `Data/` in das Spielverzeichnis legen. `Wow-ARM64.exe` ist nicht nötig. Alternativ SD-Karte, USB (MTP) oder `tools/sync_wow_to_device.sh`.
4. Von der Beta: **WoW Forever Has Launched → Upgrade Now** lädt den neuen Client (~100 MB), ergänzt `wowf` in `.build.info` und übernimmt die Einstellungen aus `_classic_beta_/WTF/Config.wtf`.

### Launcher-Einstellungen

- **Resolution:** **Nativ** (Geräteauflösung, Standard), 720p, 900p oder 1080p.
- **Render scale:** 50 % (`SET RenderScale "0.5"`) bei neuen Installationen.
- **Language:** Sprache des Launchers, dauerhaft gespeichert.
- **Community:** Buttons für die Website und den Discord-Server von Classic Forever.
- **Start + Select + L2 + R2** (oder Back) auf dem Ladebildschirm öffnet erneut die Einstellungen.


---

## Privater Server (Classic Forever)

Dieser Fork ist für den Server **Classic Forever** gemacht: [Web](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

Der Patcher (`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`) ist ein Port von `classic-forever-linux.py`. Er läuft als Thread innerhalb der App und greift direkt auf `/proc/<pid>/mem` zu — ohne Root. Er schreibt `BetaSuspendedTest.wtf` mit deinem Portal, findet den Spielprozess und ersetzt beim Betreten eines Realms Schlüssel 8 des 12-Eintrags-Speichers; er setzt ihn erneut, falls der Client ihn wiederherstellt.

Standardwerte: `auth.gpon.com.co` und der zugehörige Schlüssel. Für den eigenen Server `classic-forever.properties` im Spielverzeichnis anlegen:

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` sind 64 Hex-Zeichen des **öffentlichen** Schlüssels (32 Bytes). Ungültige Werte fallen auf den Standard zurück.

**Diagnose:** `logs/classic-forever.log` im Spielordner und logcat mit Tag `ClassicForever`. `Waiting for Wow-ARM64.exe...` = Prozess nicht gefunden (alle 10 s werden Kandidaten geloggt); `Memory access denied` = SELinux blockiert den Zugriff; `Waiting for realm connection` nach dem Betreten der Welt = `KNOWN_KEYS` aus dem aktuellen Skript aktualisieren. Wenn der erste Realm-Versuch scheitert, erneut versuchen, **ohne das Spiel zu schließen**.

---

## Update, Fehlerbehebung, Build

Der In-App-Updater nutzt Blizzards öffentliche CDNs ohne PC. Bei einer neuen Build erscheint die Update-Karte (mit **Launch Anyway (Outdated)**). Alternative: `tools/sync_wow_to_device.sh`.

| Symptom | Lösung |
| :--- | :--- |
| *CAS system was unable to initialize* | `.build.info` oder `_forever_/.flavor.info` fehlt. |
| Keine Realms | Client veraltet — Update antippen. |
| Tastatur erscheint nicht | Gboard beenden und **Keyboard** erneut öffnen. |
| Rückkehr zum Launcher nach dem Start | `_forever_/Errors/` prüfen. |
| Privater Server verbindet nicht | `logs/classic-forever.log` prüfen: `Found game`, `Server public key written`, `READY`. |

Build: JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh`, dann `./gradlew assembleModernRelease` bzw. `assembleModernDebug`.

---

## Danksagungen und Lizenz

Basiert auf **[GameNative](https://github.com/utkarshdalal/GameNative)** (GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, dem Bundle der **WoW Forever RP6**-Community (Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)** und **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**. Details in [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES). Lizenz GPL-3.0, siehe [`LICENSE`](LICENSE).
