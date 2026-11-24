# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** is a fork of WoW Forever for Android built specifically for playing on the **Classic Forever private server**. It runs **World of Warcraft: Forever** on Snapdragon handhelds as a regular Android app: tap the icon, press Play, and the game starts.

The app uses Blizzard's own **Windows ARM64** client, so the game runs natively on the phone's CPU. Wine translates Windows calls, while DXVK and a patched Turnip Vulkan driver render on the Adreno GPU. Nothing is emulated as x86.

> Unofficial community project. Not affiliated with or endorsed by Blizzard Entertainment. This repo and its releases contain **no** Blizzard game files.

> 💬 [Classic Forever on Discord](https://discord.gg/zBmq68jby) · 🌐 [Project website](http://classicforever.duckdns.org/)

---

## What this fork adds

- **Built-in private-realm patcher.** A Kotlin port of `classic-forever-linux.py`: it points the client at your auth portal via `-config BetaSuspendedTest.wtf` and replaces the public key in the game's memory. Server host and key are configurable through `classic-forever.properties` without rebuilding the APK.
- **Language selector.** English, Russian, Spanish, French, German, Brazilian Portuguese, Simplified Chinese, Japanese, Korean, plus "System Default". The choice is saved and applied on every launch.
- **On-device logs.** `logs/classic-forever.log` (patcher) and `logs/app.log` (app) inside the game folder, next to `Data/`. Both rotate at 2 MB and can be read with any file manager — no adb needed.
- **Community buttons.** Website and Discord shortcuts right on the launcher screen.
- **Practical launcher layout.** Settings grouped by purpose, app version shown in the header, clearly separated file/account actions.
- **Native resolution by default.** New installs start at the device's real screen resolution with `RenderScale` set to 50% for better performance. These values are written **only on first launch**; anything you change later in-game is preserved across restarts.
- **Monochrome neon UI** for the launcher and boot splash.

Everything else is the same proven stack: direct ARM64 launch and in-app game updates.

---

## Supported devices

| Device | Status |
| :--- | :--- |
| AYN Thor (Snapdragon 8 Gen 2 / Adreno 740) | Tested, reaches the game world with controller support |
| Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) | Tested by the original community setup |
| AYN Odin 2 / Mini (Snapdragon 8 Gen 2) | Expected to work |
| OnePlus 15r (Snapdragon 8 Gen 5 / Adreno 829) | Community-tested in-world |
| AYN Odin Portal (Snapdragon 8 Elite / Adreno 830) | Supported via bundled Turnip v32 driver |
| RedMagic 11 Pro (Snapdragon 8 Elite / Adreno 840) | Community-tested in-world |
| Poco F7 (Snapdragon 8s Gen 4 / Adreno 825) | Community-tested in-world |
| Samsung Galaxy S23/S24/S25 / Z Fold series | Supported (with automatic Samsung UBWC optimization) |

Requirements:

- Snapdragon 8 series (Adreno 7xx or 8xx). Bundled drivers for Adreno 740 and Turnip v32 for Snapdragon 8 Elite / Adreno 8xx; the right one is selected automatically.
- Android 10 or newer, 64-bit.
- About **60 GB** free for game data (internal storage or SD card), plus about 4 GB of internal storage for the app and its Windows environment.
- Wi-Fi. A computer is optional: it only speeds up the first install.

---

## Installation

### 1. Install the app

Download `Classic-Forever.apk` from the [latest release](https://github.com/legustoss/classic-forever-android/releases/latest) and install it. The package name is `app.wowforever`, so it can sit alongside GameNative or Winlator.

> Updating from an earlier **WoW Forever** build? Use the same signing key. If the key changes, Android will refuse the update and you'll need to uninstall first — game data on the card is kept.

Releases from v2.4.5 on have a build provenance attestation: a signed record that this repository's release workflow built that exact file, and from which commit. To check a download, run `gh attestation verify WoW-Forever.apk --repo jaredgei/wow-forever-android` with the [GitHub CLI](https://cli.github.com).

### 2. Install the game

Open **Classic Forever** and grant **All files access** when asked. The launcher shows **Install World of Warcraft** with a default location of `/storage/emulated/0/WoW Forever/`. Use **Change Install Location** to pick another folder (e.g. on an SD card; a non-empty folder gets a new `WoW Forever` subfolder), then tap **Download & Install**.

The app downloads the ARM64 client and CASC indexes (about 250 MB) from Blizzard's CDN, verifying every hash. The rest of the game streams as you play and is stored on the device. First visits to a zone take longer; the data eventually grows to about 50 GB.

#### Copy the game from a computer instead

If WoW Forever is already installed through Battle.net on a Mac or PC, copy its data to skip streaming. Put these two items into the game root (`WoW Forever/` in internal storage or on the SD card):

```
WoW Forever/
├── .build.info                   <- from the Blizzard install root
└── Data/                         <- the whole Data folder
```

- `Data/` is identical across platforms, so Mac, Windows and ARM installs all work.
- `.build.info` is hidden — copy it too. Without it the game fails with *"CAS system was unable to initialize"*.
- You don't need `Wow-ARM64.exe`: the launcher downloads the matching ARM64 client and checks its hashes.
- **Locate Existing Files** can point at any other folder.

Ways to copy: SD card, USB file transfer (MTP), or the ADB script:

```bash
WOW_SRC="/Applications/World of Warcraft" tools/sync_wow_to_device.sh
```

#### Upgrading from the WoW Forever beta

Blizzard replaced the beta (`wow_classic_beta`, `_classic_beta_/`) with the full release (`wowf`, `_forever_/`). Both share the same archives, so nothing needs re-copying. The launcher offers **WoW Forever Has Launched → Upgrade Now**: it downloads the new ARM64 client (about 100 MB), adds `wowf` to `.build.info`, and carries over settings from `_classic_beta_/WTF/Config.wtf`. The `_classic_beta_/` folder can then be deleted.

### 3. Play

Once files and credentials are configured, the app launches straight into the game. Otherwise it stops at the setup screen.

- **Setup screen:** hold **Start + Select + L2 + R2** (or press Back) on the loading splash to cancel auto-boot and return to setup.
- **Resolution:** **Native** (device resolution, the default), 720p, 900p or 1080p.
- **Render scale:** new installs use 50% (`SET RenderScale "0.5"`).
- **Language:** launcher UI language; saved across launches.
- **Community:** Website and Discord buttons open the Classic Forever site and Discord server.
- **Add-on:** download a `.zip`, tap **Add-on** and pick it. It is installed into `_forever_/Interface/AddOns`.

### Controls and signing in

- **Controller:** built-in handheld controllers work through WoW's own gamepad mode.
- **In-game menu:** press Back for the sidebar — keyboard, on-screen controls, performance overlay, exit.
- **Keyboard:** opens the Android keyboard (on the lower screen of dual-screen devices); symbols like `@` and paste work.
- **Battle.net auto-login:** credentials are stored encrypted via Android Keystore. On boot the launcher writes `_forever_/login.txt`, and the client signs in natively. **Forget Login** clears both.
  - Each install has its own Windows machine ID and `DESKTOP-XXXXXXX` name, kept across updates.

---

## Private realm (Classic Forever)

This fork is made for the **Classic Forever** server: [website](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

The logic of `classic-forever-linux.py` is ported to Kotlin in `app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`. Android has no Python runtime in the app, and the Wine processes are children of the app, so the patcher runs as a background thread and accesses `/proc/<pid>/mem` directly — no root, no code or disk patches.

On each launch the app:

1. Writes `_forever_/WTF/BetaSuspendedTest.wtf` from the normal `Config.wtf`, with `SET portal` pointing at the private auth host, and passes `-config BetaSuspendedTest.wtf` to the client.
2. Waits for the game process, scans its anonymous heap for the 12-entry public-key store, and replaces entry 8 with the server's public key when the client connects to a realm.
3. Re-applies the key if the client restores the original one, and stops when the game exits.


`key` is the 64-character hex **public** key (32 bytes). Missing or invalid values fall back to the built-ins.

### Diagnostics

Patcher status goes to `logs/classic-forever.log` in the game root and to logcat under the `ClassicForever` tag (`adb logcat -s ClassicForever`). General app logs go to `logs/app.log`. Both rotate into a `.old` backup at 2 MB.

- `Waiting for Wow-ARM64.exe...` — the process was not identified. Every 10 seconds `Processes mentioning the game` lists candidates (pid, comm, RSS, whether the .exe is in the memory map), so you can see whether detection or memory access is the problem.
- `Memory access denied` — SELinux is blocking `/proc/<pid>/mem`.
- `Waiting for realm connection` — process found, key store not created yet. If it persists after entering the world, update `KNOWN_KEYS` from the current `classic-forever-linux.py` and rebuild.
- If the first realm login fails, retry **without** closing the game.

---

## Updating the game

The in-app updater talks directly to Blizzard's public CDNs — no PC or cable needed. On startup the launcher checks the live version; if a newer build exists, auto-launch is cancelled and an update card appears (with a **Launch Anyway (Outdated)** override). **UPDATE TO {version}** downloads new ARM64 binaries and configs, syncs CASC indexes and atomically updates `.build.info`. New assets then stream during play.

Fallback: `tools/sync_wow_to_device.sh` mirrors a full PC install over USB.

---

## Troubleshooting

| Symptom | Fix |
| :--- | :--- |
| *CAS system was unable to initialize: no active install info entries* | `.build.info` or `_forever_/.flavor.info` is missing on the device. |
| No realms listed | Client out of date. Tap **Update** on the setup screen or restart with Wi-Fi. |
| Keyboard doesn't appear | Force-stop Gboard, then open **Keyboard** again. |
| Returns to launcher after "Launching Game…" | Check `_forever_/Errors/` on the device. |
| Wrong icon in a front-end (e.g. Cocoon) | The front-end cached the old artwork; refresh it or reinstall. |
| Private server won't connect | Check `logs/classic-forever.log` for `Found game`, `Server public key written` and `READY`. |

---

## Building from source

Requirements: JDK 17 and the Android SDK (build-tools, platform-tools, NDK 27.3.13750724).

```bash
tools/fetch_components.sh           # Wine/Proton, DXVK, Turnip archives (~130 MB, not in git)
./gradlew assembleModernRelease     # app/build/outputs/apk/modern/release/app-modern-release.apk
./gradlew assembleModernDebug       # unminified debug build, faster to iterate on
```

`fetch_components.sh` downloads the three runtime archives from this repo's `components-v1` release and verifies them against `tools/components.sha256`. If you already have them: `tools/fetch_components.sh /path/to/components`.

Release builds are signed with `app/keystores/keystore.properties` when present (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`), otherwise with the debug key. Different keys can't update each other.

---

## Credits and how it works

This project packages other people's work into a single-purpose app. None of it would exist without:

- **[GameNative](https://github.com/utkarshdalal/GameNative)** by Utkarsh Dalal and contributors (GPL-3.0): the Android app, Wine container management and X server are based on v1.2.1. GameNative in turn builds on **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, **[Winlator Cmod](https://github.com/coffincolors/winlator)** and the **[Bionic Vulkan wrapper](https://github.com/leegao/bionic-vulkan-wrapper)**.
- **The WoW Forever RP6 community bundle**, which first got the beta running on a Retroid Pocket 6 and supplied three custom runtime components:
  - **Proton 11 ARM64EC** from [The412Banner/proton-wine](https://github.com/The412Banner/proton-wine/tree/e5fa703ed7f7329e20d7ede481ab185cf8b1a8b2).
  - **DXVK 2.4.1 (aarch64)** from [doitsujin/dxvk](https://github.com/doitsujin/dxvk/tree/0cf05780abd7250c2cd713b7749cf32180157cf5).
    - **Mesa Turnip (Adreno 740)** from Mesa [`fe067b17d9`](https://github.com/mirror/mesa/tree/fe067b17d9).
  - **Mesa Turnip (Adreno 8xx / Snapdragon 8 Elite)** by **[arusiasotto](https://github.com/arusiasotto/wow-forever-a840)** from **[whitebelyash/mesa-unified](https://github.com/whitebelyash/mesa-unified)** (`turnip/gen8` v32).
- **[Wine](https://www.winehq.org/)**, **[Proton](https://github.com/ValveSoftware/Proton)**, **[DXVK](https://github.com/doitsujin/dxvk)**, **[Mesa](https://mesa3d.org/)**, **[FEX-Emu](https://github.com/FEX-Emu/FEX)**, **[box64](https://github.com/ptitSeb/box64)** and **[PulseAudio](https://www.freedesktop.org/wiki/Software/PulseAudio/)**.
- **Blizzard Entertainment** for World of Warcraft and its Windows ARM64 client. The app icon and splash art come from the official [WoW Forever page](https://worldofwarcraft.blizzard.com/en-us/forever) and remain Blizzard's property.

Full third-party license details are in [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES).

### What changed from WoW Forever for Android

- **Private-realm patcher**, language selector, file logging, community links, practical launcher layout, native-resolution default with 50% render scale, and a monochrome neon theme.
- The base project's own changes from GameNative are preserved: standalone package/identity, direct ARM64 launch, pre-configured container, game file setup, keyboard and package-name fixes, in-app game updater, and a lean runtime.

## License

GPL-3.0, same as GameNative. See [`LICENSE`](LICENSE). Bundled runtime components carry their own licenses (LGPL, MIT and others) — see `THIRD_PARTY_NOTICES`.
