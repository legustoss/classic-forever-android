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
| 1.1 | Online count from a real source | **WAITING** | `server.status.source=http` + URL returning `{"online": 12, ...}`; UI shows "In-game online: 12" or "—" | Server developer has not provided data access; wait for a response. Meanwhile the counter stays "—" |
| 1.2 | Dynamic supported client build from that endpoint | **PLANNED** | `client_build` parsed and used as compatibility requirement; unit tests for parse/boundaries; download logic untouched | Same endpoint as 1.1 |
| 1.3 | Step 6 sign-off | **BLOCKED** | Same as 0.2 | Device/screenshots |
| 1.4 | Step 7 device validation | **BLOCKED** | `device report` issues with GPU, driver id, in-world result; then flip Adreno 750/735/825/850 from "pending validation" | Real devices |
| 1.5 | Benchmark overlay / measurement | **PLANNED** | Scenarios from `docs/benchmarks.md` executed on device; results recorded with device/app/driver; no estimates | Device + profiling time |

## Phase 2 — Quality and consistency

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 2.1 | Full unit-test suite | **DONE** | 391 tests, 0 failures, 12 skipped. Fixed the config filter and cross-platform registry replacement; 10 POSIX-only tests skipped on Windows | — |
| 2.2 | `lintModernDebug` errors and warnings triaged | **DONE** | 0 errors, 819 findings (809 warnings, 10 info). Fixed manifest features, named constants and API-29 guards; `MissingTranslation` downgraded in `app/lint.xml` because only 9 languages are supported | — |
| 2.3 | Locale sync | **DONE** | All 9 `strings_launcher.xml` files carry the same set of keys and equivalent labels | — |
| 2.4 | `LogNotTimber` in launcher-owned Kotlin | **DONE** | 24 warnings fixed in controller/XServer code; 286 upstream `com.winlator` Java warnings remain as backlog | — |

## Phase 3 — Project maturity (P3)

| # | Item | State | Acceptance criteria |
|---|---|---|---|
| 3.1 | Second maintainer with merge rights | **DROPPED** | User decided it is not needed right now; revisit only on request |
| 3.2 | Rebase on GameNative upstream | **REVIEWED / NO APPLICABLE WINS** | Read-only diff 2026-11-24: nearest fork `jaredgei/wow-forever-android` has only a version bump since our base; GameNative has no shared merge base and its recent perf work is gamescope/shader-cache/binary manifests not present here. No code ported; binary component changes remain forbidden without explicit approval |
| 3.3 | SBOM / component inventory | **DONE** | `docs/sbom.md` — manually maintained inventory of toolchain, Gradle deps, runtime archives and proprietary components; no binaries added |
| 3.4 | Component update automation | **PLANNED** | Script checks upstream releases and opens a PR proposal; never auto-merges |

## Phase 4 — Optional (P4)

- Support for other private servers (only after the current one is stable).
- Sponsorship/funding, if the community asks for it.

## Phase 5 — LSFG (user-provided DLL)

| # | Item | State | Acceptance criteria |
|---|---|---|---|
| 5.1 | LSFG with a user-imported `Lossless.dll` | **DONE** | The inherited `LsfgVkManager` no longer assumes a Steam install: the user imports their own DLL from launcher Settings, it is stored in app-private `files/lsfg/Lossless.dll`, copied into the container at launch, and `isArmed()` accepts it. No DLL is bundled in the APK |
| 5.2 | Explicit launch only | **DONE** | Removed implicit `launchGame()` calls on launch, after install and after update. `WoWLauncherState.shouldAutoLaunch` defaults to `false`, so no boot splash/auto-start |
| 5.3 | Whole Quick Menu categorisation | **DONE** | Shared `QuickMenuCategoryHeader` with divider across HUD, LSFG, Tools and Controller tabs; Controller split into Input / Touch / Motion; LSFG flow scale and performance mode grouped; Effects already had category headers. No state, focus or QuickMenu() signature changes |

## Decision requests / blockers

1. **0.2 / 1.3:** user cannot provide screenshots or a device. Remains an accepted verification gap; do not mark as passed.
2. **1.1 / 1.2:** awaiting the server developer's response about data access. If an endpoint appears, send URL + sample JSON. Ephemeral Discord replies cannot be used.
3. **1.4:** needs real devices for in-world validation.

Resolved: 0.3 (release `v0.1.1` already published), 0.4 (locales synchronised).

## Next action

Phase 0, Phase 2 and Phase 5 are closed or hard-blocked: 2.1 (tests), 2.2 (lint), 2.3 (locales) and 5.1 (LSFG DLL import) are done. Top remaining lint warnings are upstream backlog and are not release blockers: `LogNotTimber` (286 in `com.winlator`, after fixing our 24), `MissingTranslation` (102), `TypographyEllipsis` (90), `UnusedResources` (84), `GradleDependency` (54). 3.1 is dropped, 3.3 is done, 3.2 is explained but needs approval. Items 0.2/1.3, 1.1/1.2 and 1.4 remain blocked by device/data availability. Next: either approve a read-only upstream diff (3.2), pick a lint-warning category, or wait for the server endpoint and resume 1.1/1.2.
