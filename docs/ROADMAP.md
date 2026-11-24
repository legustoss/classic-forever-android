# Development roadmap

Snapshot: **2026-11-24**, version **0.1.1** (`versionCode = 2`). This document is the single source of truth for "what next"; `AGENTS.md` points to it. Update it whenever an item changes state.

Legend: **DONE** · **IN PROGRESS** · **BLOCKED** (needs input/device) · **PLANNED**.

## Phase 0 — Stabilise the current release

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 0.1 | Three-screen launcher (Main / Settings / FirstInstall), compact landscape layout, localized `wow_back` | **DONE** | Commits `1670a53`, `ced8069`, `180d503`, `7bbf87e`; debug + release builds pass | — |
| 0.2 | Visual sign-off at 360×640, 412×915, 800×1280, 1280×800 | **BLOCKED** | No overflow/clipping in portrait or landscape; buttons reachable at all four sizes | No `adb`/emulator; user cannot provide screenshots. Keep as a documented verification gap — never claim it passed |
| 0.3 | Publish release `v0.1.1` | **DONE** | Release already exists on GitHub with tag `v0.1.1` | — |
| 0.4 | Locale consistency after the RU-only label shortenings | **DONE** | All 9 `strings_launcher.xml` files now carry the shortened pattern: booting/readiness/login/play/install/driver strings | — |

## Phase 1 — Close the original AGENTS.md steps

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 1.1 | Online count from a real source | **HIDDEN / WAITING** | Counter is hidden until a source returns real data; no placeholder is shown. Server developer still has not provided access | Discord `/status` is ephemeral and cannot be read by the launcher; an HTTP endpoint or bot-side proxy is required |
| 1.2 | Dynamic supported client build from that endpoint | **PLANNED** | `client_build` parsed and used as compatibility requirement; unit tests for parse/boundaries; download logic untouched | Same endpoint as 1.1 |
| 1.3 | Step 6 sign-off | **BLOCKED** | Same as 0.2 | Device/screenshots |
| 1.4 | Step 7 device validation | **BLOCKED** | `device report` issues with GPU, driver id, in-world result; then flip Adreno 750/735/825/850 from "pending validation" | Real devices |
| 1.5 | Benchmark overlay / measurement | **PLANNED** | Scenarios from `docs/benchmarks.md` executed on device; results recorded with device/app/driver; no estimates | Device + profiling time |

## Phase 2 — Quality and consistency

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 2.1 | Full unit-test suite | **DONE** | 391 tests, 0 failures, 12 skipped. Fixed the config filter and cross-platform registry replacement; 10 POSIX-only tests skipped on Windows | — |
| 2.2 | `lintModernDebug` errors and warnings triaged | **DONE** | 0 errors, 773 findings; fixed manifest features, named constants and API-29 guards; `MissingTranslation` downgraded in `app/lint.xml` because only 9 launcher languages are supported | — |
| 2.3 | Locale sync | **DONE** | All 9 `strings_launcher.xml` files carry the same set of keys and equivalent labels | — |
| 2.4 | `LogNotTimber` in launcher-owned Kotlin | **DONE** | 24 warnings fixed in controller/XServer code; 286 upstream `com.winlator` Java warnings remain as backlog |
| 2.5 | `DefaultLocale` correctness warnings | **DONE** | 26 warnings fixed: internal identifier lowercasing uses `Locale.ROOT`, four `String.format("%.2fx")` use `Locale.US` | — |

## Phase 3 — Project maturity (P3)

| # | Item | State | Acceptance criteria |
|---|---|---|---|
| 3.1 | Second maintainer with merge rights | **DROPPED** | User decided it is not needed right now; revisit only on request |
| 3.2 | Rebase on GameNative upstream | **REVIEWED / NO APPLICABLE WINS** | Read-only diff 2026-11-24: nearest fork `jaredgei/wow-forever-android` has only a version bump since our base; GameNative has no shared merge base and its recent perf work is gamescope/shader-cache/binary manifests not present here. No code ported; binary component changes remain forbidden without explicit approval |
| 3.3 | SBOM / component inventory | **DONE** | `docs/sbom.md` — manually maintained inventory of toolchain, Gradle deps, runtime archives and proprietary components; no binaries added |
| 3.4 | Component update automation | **PLANNED** | Script checks upstream releases and opens a PR proposal; never auto-merges |
| 3.5 | Versioning policy | **DONE** | Patch = fixes, Minor = new features/major UI, Major = stable 1.0; next release planned as `v0.2.0` because of LSFG. Documented in `docs/release-checklist.md` |
| 3.6 | Component provenance / update watchlist | **REVIEWED** | `components-v1` is from 2026-09-30; no components-v2 tag. Candidates: DroidDeck-Proton 11.0-12.8 arm64ec (v16, perf-relevant Wine fixes, but built for DroidDeck runtime pack and not device-tested upstream), DXVK 3.1.1 (no aarch64 EC artifact), Turnip unchanged. No binary adopted; details in `docs/performance-candidates.md` |

## Phase 4 — Optional (P4)

- Support for other private servers (only after the current one is stable).
- Sponsorship/funding, if the community asks for it.

## Phase 5 — LSFG (user-provided DLL)

| # | Item | State | Acceptance criteria |
|---|---|---|---|
| 5.1 | LSFG with a user-imported `Lossless.dll` | **DONE** | The inherited `LsfgVkManager` no longer assumes a Steam install: the user imports their own DLL from launcher Settings, it is stored in app-private `files/lsfg/Lossless.dll`, copied into the container at launch, and `isArmed()` accepts it. No DLL is bundled in the APK |
| 5.2 | Explicit launch only | **DONE** | Removed implicit `launchGame()` calls on launch, after install and after update. `WoWLauncherState.shouldAutoLaunch` defaults to `false`, so no boot splash/auto-start |
| 5.3 | Whole Quick Menu categorisation | **DONE** | Shared `QuickMenuCategoryHeader` with divider across HUD, LSFG, Tools and Controller tabs; Controller split into Input / Touch / Motion; LSFG flow scale and performance mode grouped; Effects already had category headers. No state, focus or QuickMenu() signature changes |
| 5.4 | On-device check: no auto-launch + LSFG + categorised Quick Menu | **IN PROGRESS** | User confirmed everything works except a hitch when switching the LSFG multiplier; device log now shows `Launcher back: FIRST_INSTALL -> MAIN`, so the back button path is exercised. Re-test after 5.5 | Awaiting user re-test |
| 5.5 | LSFG multiplier hitch | **DONE** | `LsfgQuickMenuHelper.applySettings` now persists and hot-reloads on a background executor; UI state updates immediately | — |
| 5.6 | Back button on tall launcher screens | **DONE** | Scrollable launcher content is top-aligned instead of centered, so the top bar is no longer clipped/unclickable on FirstInstall/Settings |
| 5.7 | Launch status messages | **DONE** | Hardcoded English container/Proton/DXVK/driver status texts moved to `strings_launcher.xml` in all 9 languages |
| 5.8 | FPS target change stutter | **DONE** | LSFG settings apply is debounced (180 ms, latest wins) so rapid FPS steps no longer queue repeated native config/renderer reloads |
| 5.9 | Password login dialog | **DONE** | Removed hardcoded "Battle.net" English text; dialog uses launcher strings and Wow theme |
| 5.10 | Launcher gamepad navigation | **DONE** | `rememberGamepadConnected()` observes hot-plug without touching game input; hints with icons shown when connected; BUTTON_A confirms and BUTTON_B goes back in launcher only |
| 5.11 | Launcher component manager | **DONE** | Settings → Components lists built-in recommended components, downloadable v16 test Proton with SHA-256 check, SAF import for custom files, and reset-to-bundled. Overrides live in app-private `files/components/`; `tools/` untouched |
| 5.12 | 32-bit Wine trim experiment | **PLANNED** | Repack components-v1 without unused i386 files as a test APK; no adoption until device comparison |

## Decision requests / blockers

1. **0.2 / 1.3:** user cannot provide screenshots or a device. Remains an accepted verification gap; do not mark as passed.
2. **1.1 / 1.2:** awaiting the server developer's response about data access. If an endpoint appears, send URL + sample JSON. Ephemeral Discord replies cannot be used.
3. **1.4:** needs real devices for in-world validation.

Resolved: 0.3 (release `v0.1.1` already published), 0.4 (locales synchronised).

## Next action

Phase 0, Phase 2 and Phase 5 are closed or waiting on the user: 2.1 (tests), 2.2 (lint), 2.3 (locales), 5.1–5.3 (LSFG DLL, explicit launch only, Quick Menu categories) are done; 5.4 awaits the phone test report. Remaining lint warnings are upstream backlog and not release blockers: `LogNotTimber` (286 in `com.winlator`), `MissingTranslation` (102), `TypographyEllipsis` (90), `UnusedResources` (84), `GradleDependency` (54). 3.1 is dropped, 3.2 was reviewed with no applicable wins, 3.3 is done, 3.4 needs explicit approval because it touches `tools/`.

Hard-blocked: 0.2/1.3 (no device/screenshots), 1.1/1.2 (no server endpoint), 1.4 (no devices), 1.5 (no profiling device). Remaining lint categories (`TypographyEllipsis`, `UnusedResources`, `ObsoleteSdkInt`) are cosmetic or touch broad runtime code; not worth the regression risk right now. Unblocked options: approve 3.4 automation (touches `tools/`). Next release is planned as **v0.2.0**; prepare it only after the user re-tests the LSFG multiplier fix.
