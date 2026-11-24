# Development roadmap

Snapshot: **2026-11-24**, version **0.1.1** (`versionCode = 2`). This document is the single source of truth for "what next"; `AGENTS.md` points to it. Update it whenever an item changes state.

Legend: **DONE** · **IN PROGRESS** · **BLOCKED** (needs input/device) · **PLANNED**.

## Phase 0 — Stabilise the current release

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 0.1 | Three-screen launcher (Main / Settings / FirstInstall), compact landscape layout, localized `wow_back` | **DONE** | Commits `1670a53`, `ced8069`, `180d503`, `7bbf87e`; debug + release builds pass | — |
| 0.2 | Visual sign-off at 360×640, 412×915, 800×1280, 1280×800 | **BLOCKED** | No overflow/clipping in portrait or landscape; buttons reachable at all four sizes | No `adb`/emulator in the environment; need a device or user screenshots |
| 0.3 | Publish release `v0.1.1` | **BLOCKED** | Tag `v0.1.1` matches `versionName`; workflow signature check passes; APK downloadable | User must run Actions → Release; four signing secrets must be configured |
| 0.4 | Locale consistency after the RU-only label shortenings | **IN PROGRESS** | Decide whether shortened RU strings (`wow_play`, `wow_readiness`, `wow_booting`, `wow_install_card_install`, `wow_driver_pending`) should be mirrored in EN and the other 7 locales | Decision from user; do not silently rewrite translations |

## Phase 1 — Close the original AGENTS.md steps

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 1.1 | Online count from a real source | **BLOCKED** | `server.status.source=http` + URL returning `{"online": 12, ...}`; UI shows "In-game online: 12" or "—" | Discord `/status` is ephemeral; Discord widget returns 403; need an HTTP endpoint or server-side proxy |
| 1.2 | Dynamic supported client build from that endpoint | **PLANNED** | `client_build` parsed and used as compatibility requirement; unit tests for parse/boundaries; download logic untouched | Same endpoint as 1.1 |
| 1.3 | Step 6 sign-off | **BLOCKED** | Same as 0.2 | Device/screenshots |
| 1.4 | Step 7 device validation | **BLOCKED** | `device report` issues with GPU, driver id, in-world result; then flip Adreno 750/735/825/850 from "pending validation" | Real devices |
| 1.5 | Benchmark overlay / measurement | **PLANNED** | Scenarios from `docs/benchmarks.md` executed on device; results recorded with device/app/driver; no estimates | Device + profiling time |

## Phase 2 — Quality and consistency

| # | Item | State | Acceptance criteria | Blocked by |
|---|---|---|---|---|
| 2.1 | Full unit-test suite | **PLANNED** | 12 known Windows-only failures (symlinks/paths/config) triaged: fixed or documented as environment-specific | Separate task; never mixed with feature work |
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

## Decision requests (need user input)

1. **0.2 / 1.3:** provide four screenshots (or a device with `adb`) for responsive sign-off.
2. **0.3:** trigger the `v0.1.1` release and confirm the signing secrets exist.
3. **0.4:** should the shortened Russian labels be applied to English and the other locales, or stay RU-only?
4. **1.1 / 1.2:** is there a public HTTP status endpoint (or can the Discord bot expose one)? If yes, send URL + sample JSON. Ephemeral Discord replies cannot be used.

## Next action

The first unblocked action is **0.4** — but it requires the locale decision above. Items 0.2, 0.3, 1.1 and 1.4 are hard-blocked by device/credentials/data source. Once the user answers, update this table, then execute exactly one item in one logical commit.
