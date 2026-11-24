# Session state snapshot

Snapshot: 2026-11-24. Version in `app/build.gradle.kts`: 0.1.1 (`versionCode = 2`). Next planned release: **v0.2.0** (not prepared yet).

## Last verified state

- `assembleModernDebug`: BUILD SUCCESSFUL.
- `testModernDebugUnitTest`: 404 tests, 0 failures, 12 skipped.
- `lintModernDebug`: 0 errors, 835 findings; only 2 `ObsoleteSdkInt` items remain (manifest/mipmap, out of scope). Backlog remains, see ROADMAP.
- No release build — user asked to run it only when preparing a real release.

## Just completed

- Launcher: three screens, top bar reachable, back button works, statuses localized.
- Login dialog: no hardcoded "Battle.net", styled as launcher.
- Quick Menu: categorized, gamepad focus hints, FPS debounce (180 ms).
- Launcher gamepad: `rememberGamepadConnected()`, A confirms, B back, no game-input changes.
- Components manager in Settings: built-in recommended components, v16 test Proton download with SHA-256, SAF import, reset. Overrides stored in `files/components/`; bundled assets remain fallback; `tools/` untouched.
- Proton v16 works on user's device but is **not approved** as recommended; user will compare on two devices.
- 32-bit trim experiment APK built from proven components-v1.
- Launcher portrait rhythm restored with a deterministic 64 dp top spacer; Settings/FirstInstall back bar row is fully clickable and maps gamepad A via `gamepadConfirm`.
- Windows ADB guide: `docs/adb-testing.md`; `adb` added to user PATH (`%LOCALAPPDATA%\Android\Sdk\platform-tools`).
- First device measurement recorded in `docs/performance-results.md`: v16, city running ~60 s, reset at 17:03:50. CPU p50 21 ms / p90 36 ms / p95 46 ms / p99 93 ms; GPU p90+ shows 4950 ms sentinel (timing unavailable). Baseline and stripped are NOT measured on that route yet.
- Obsolete `SDK_INT` guards removed from 8 Kotlin/Java files; multi-digit version-code suffix bug in `WineInfo` fixed earlier.

## Test APKs (gitignored, in `test-apks/`)

- `Classic-Forever-baseline.apk` — current code, original components-v1.
- `Classic-Forever-Proton-v16-TEST.apk` — v16 Proton candidate (not approved).
- `Classic-Forever-Proton-stripped-TEST.apk` — original Proton with 1013 i386/lib32 entries removed; ~29 MB smaller APK.

Always clear app data before switching APKs: extracted Proton is only replaced when the app-private prefix is missing `profile.json` or `bin/wine`.

## Waiting / blocked

- Server developer response for online count and supported client build; online counter is hidden, client version check is no-op without bounds.
- User's two-device comparison of baseline vs v16 vs stripped builds; currently testing v16 and temporarily away from the PC, so no new adb captures until return.
- On-device sign-off at 360×640 / 412×915 / 800×1280 / 1280×800; ADB is now available but visual review still needs the user.
- Adreno 750/735/825 device validation.

## Hard rules to remember

- `AGENTS.md` is local-only and gitignored; do not commit it.
- Do not modify `tools/`, keystores, workflows, `gradle.properties` or `settings.gradle.kts` without explicit approval.
- No binaries/secrets/personal data in git.
- Never auto-launch the game; only launcher buttons.
- No invented FPS numbers; "requires benchmark" until measured.
- No release build unless preparing a release.

Source of truth for next steps: [ROADMAP.md](ROADMAP.md).
