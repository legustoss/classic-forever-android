# ADB testing on Windows

How to connect an Android device to a Windows PC and compare test APKs without a full release build.
Commands are written for **Windows PowerShell** and can be pasted as-is. The app package is `app.wowforever`.

For what to measure, see [benchmarks.md](benchmarks.md). For supported GPUs and driver ids, see [devices.md](devices.md).

## 1. Install platform-tools

`adb` ships in the Android SDK Platform-Tools, separately from Android Studio.

1. Download **SDK Platform-Tools for Windows** from <https://developer.android.com/tools/releases/platform-tools>.
2. Unzip it to a folder without spaces, for example `C:\platform-tools`.
3. Add it to the user PATH (once per PC):

```powershell
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\platform-tools", "User")
```

4. Open a **new** PowerShell window. A PATH change does not reach terminals that are already open.

```powershell
adb version
```

On the Windows PC used for these tests, platform-tools is already on the user PATH. If an existing terminal still says
`adb` is not recognized, close it and open a new one, or call the binary by full path:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" version
```

## 2. Enable USB debugging on the device

1. **Settings > About phone** and tap **Build number** seven times to unlock Developer options.
2. **Settings > System > Developer options** and turn on **USB debugging**.
3. Connect the device with a data-capable USB cable and pick **File transfer** (or **USB** / MTP) if the device asks for a mode.

## 3. Authorize the PC

The first connection shows **Allow USB debugging?** with an RSA fingerprint.

- Tick **Always allow from this computer** and tap **Allow**.
- If the prompt never appears, unplug and replug the cable, run `adb kill-server` then `adb start-server`, or revoke
  authorizations in **Developer options > Revoke USB debugging authorizations**.

## 4. Verify the connection

```powershell
adb devices
```

```
List of devices attached
XXXXXXXXXX      device
```

The serial number is different on every device. `device` means ready. `unauthorized` means the RSA prompt was not
accepted; `offline` means the cable, port or USB mode is wrong. Replug or run `adb reconnect`.

With more than one device attached, add `-s <serial>` to every command:

```powershell
adb -s XXXXXXXX shell getprop ro.product.model
```

Record these values for the benchmark header:

```powershell
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.board.platform
```

## 5. Install a test build

The test APKs are gitignored and are not part of a normal checkout. The three builds are:

| File | Build |
| :--- | :--- |
| `Classic-Forever-baseline.apk` | reference build |
| `Classic-Forever-Proton-v16-TEST.apk` | v16 test build |
| `Classic-Forever-Proton-stripped-TEST.apk` | stripped test build |

### Clear app data before switching builds

Compare builds from the same starting state. Skip this on the first install. With the previous build still installed:

```powershell
adb shell pm clear app.wowforever
```

`pm clear` resets the app to a fresh install: login, preferences, driver choice, the Wine container and its cached
components are all removed. It does **not** delete the game folder on shared storage,
`/storage/emulated/0/WoW Forever/`, so the client files stay in place and do not need re-copying. The first launch after
a clear recreates the container and re-extracts the bundled components, so allow extra setup time. Uninstalling the app
also leaves the shared game folder alone.

### Install

Run the commands from the folder that contains `test-apks\`, one build at a time:

```powershell
adb install -r test-apks\Classic-Forever-baseline.apk
```

```powershell
adb install -r test-apks\Classic-Forever-Proton-v16-TEST.apk
```

```powershell
adb install -r test-apks\Classic-Forever-Proton-stripped-TEST.apk
```

`-r` replaces an existing install and keeps its data. Confirm which build is on the device:

```powershell
adb shell dumpsys package app.wowforever | Select-String versionName
```

If the install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the APKs were signed with different keys. Remove the
old build first, then install again:

```powershell
adb uninstall app.wowforever
```

## 6. Run a scenario

1. Clear the old log buffer: `adb logcat -c`
2. Launch the app from the device launcher. Never auto-launch the game from a script.
3. Run one scenario from [benchmarks.md](benchmarks.md) and write down the result immediately.

## 7. Collect logs

Live logcat for the patcher and launcher:

```powershell
adb logcat -s ClassicForever
```

Stop with `Ctrl+C`. To capture a run to a file, clear the buffer first and redirect:

```powershell
adb logcat -c
adb logcat -s ClassicForever > logcat-baseline.txt
```

Windows PowerShell 5.1 writes `>` output as UTF-16. If the file must be plain text, use
`adb logcat -s ClassicForever | Out-File -Encoding utf8 logcat-baseline.txt`. A one-shot dump of the current buffer is
`adb logcat -d -s ClassicForever`.

The game also writes rotating log files into the game folder. Pull them after the run:

```powershell
adb pull "/storage/emulated/0/WoW Forever/logs" logs-baseline
```

This copies `app.log` and `classic-forever.log` (plus `.old` backups) into `logs-baseline\`. The quotes matter because
of the space in `WoW Forever`.

## 8. Frame timing

`gfxinfo` measures the Android UI: the launcher, setup and menus. The game itself renders through Wine/Proton in its own
surface, so in-game frame numbers come from the in-game HUD and Perfetto, not from `gfxinfo`.

```powershell
adb shell dumpsys gfxinfo app.wowforever reset
adb shell dumpsys gfxinfo app.wowforever
```

The second command prints frame counts, jank percentage and percentiles. Add `framestats` for per-frame timestamps:

```powershell
adb shell dumpsys gfxinfo app.wowforever framestats
```

## 9. Perfetto trace

Capture a 20-second system trace while the scenario runs:

```powershell
adb shell perfetto -o /data/misc/perfetto-traces/trace.perfetto-trace -t 20s `
  sched freq idle am wm gfx view binder_driver hal dalvik input res memory
adb pull /data/misc/perfetto-traces/trace.perfetto-trace .
```

The backtick continues the command onto the next line in PowerShell. Open the pulled file at
<https://ui.perfetto.dev> and look for long tasks, CPU/GPU scheduling and missed frame deadlines. Command reference:
<https://perfetto.dev/docs/>.

## 10. Screenshots

For layout checks, capture on the device and pull the file instead of redirecting binary output:

```powershell
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png .
```

## Troubleshooting

| Symptom | Fix |
| :--- | :--- |
| `adb` is not recognized | Open a new PowerShell window after the PATH change, or use the full path to `adb.exe`. |
| `unauthorized` in `adb devices` | Accept the RSA prompt; if it never appears, revoke authorizations and replug. |
| `offline` in `adb devices` | Replug the cable, try another port, set USB mode to file transfer, run `adb reconnect`. |
| `no devices/emulators found` | The device is locked, in charge-only mode, or the cable is charge-only. |
| `more than one device/emulator` | Add `-s <serial>` from `adb devices`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Uninstall the old build first; the signing keys differ. |
| `INSTALL_FAILED_INSUFFICIENT_STORAGE` | Free space on the device; each APK is roughly 200 MB. |
| No `ClassicForever` lines | The game has not started yet; the tag appears once the patcher runs. |

## Do not commit test artifacts

`test-apks/`, `*.apk` and `*.log` are gitignored. Logs, traces and screenshots can contain account names, server
addresses and device identifiers. Keep them out of the repository and attach only the relevant excerpt to an issue.
