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
| 2.2 | `lintModernDebug` clean or warnings triaged | **PLANNED** | No new warnings introduced by launcher changes | — |
| 2.3 | Locale sync (if 0.4 says yes) | **PLANNED** | All 9 `strings_launcher.xml` files carry the same set of keys and equivalent labels | Decision from 0.4 |

## Phase 3 — Project maturity (P3)

| # | Item | State | Acceptance criteria |
|---|---|---|---|
| 3.1 | Second maintainer with merge rights | **PLANNED** | Documented in `docs/SUPPORT.md` |
| 3.2 | Rebase on GameNative upstream | **PLANNED** | Monthly diff reviewed; fork diff kept small; GPL compatibility rechecked |
| 3.3 | SBOM / component inventory | **PLANNED** | Generated from runtime archives and Gradle dependencies; stored as text, no binaries |
| 3.4 | Component update automation | **PLANNED** | Script checks upstream releases and opens a PR proposal; never auto-merges |

## Phase 4 — Optional (P4)

- Support for other private servers (only after the current one is stable).
- Sponsorship/funding, if the community asks for it.

## Decision requests / blockers

1. **0.2 / 1.3:** user cannot provide screenshots or a device. Remains an accepted verification gap; do not mark as passed.
2. **1.1 / 1.2:** awaiting the server developer's response about data access. If an endpoint appears, send URL + sample JSON. Ephemeral Discord replies cannot be used.
3. **1.4:** needs real devices for in-world validation.

Resolved: 0.3 (release `v0.1.1` already published), 0.4 (locales synchronised).

## Next action

Phase 0 items are closed or hard-blocked. **2.1 is done** (full suite green with 12 documented skips). The next unblocked item is **2.2 — triage `lintModernDebug` warnings**. Items 0.2/1.3, 1.1/1.2 and 1.4 remain blocked by device/data availability; mark 1.1/1.2 as next when the endpoint arrives.
