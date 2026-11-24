# Settings tabs and component groups — design

## Intent / why

The portrait screenshot shows component names squeezed by up to three text buttons per row, two
Proton rows both looking active, and one very long Settings card. The user wants component choice
to be obvious and the settings screen to fit before the v0.2.0 release.

## Goals

- Three internal tabs in Settings:
  1. **Лаунчер** — files (refresh, location, addon, readiness) and client/app updates.
  2. **Игра** — render resolution, language, account login/forget.
  3. **Графика** — LSFG block plus component groups.
- Three expandable component groups: **Proton**, **DXVK**, **GPU driver**. Only the driver matching
  the detected GPU is shown; unknown GPU keeps the existing warning and shows both.
- Each group has exactly **one button** at its bottom: **Импорт файла**, importing into that
  group's runtime slot. No button cluster on the right of the rows.
- Selection state stays checkbox-based and derived from the SHA-256 of the installed file.

## Non-goals

- No changes to download, checksum verification, slot mapping, driver detection, version checks,
  LSFG defaults or LSFG behavior.
- No new APK variants, no release build, no publishing, no game/input/gamepad mapping changes.

## Constraints

- Version stays 0.2.0 (`versionCode = 3`); `assembleModernDebug` is the routine gate.
- No `tools/`, keystores, workflows, gradle or dependency changes; no secrets/personal data in git.
- Keep existing colors, top bar, system BackHandler, bottom back button and measured centering.
- All new UI strings go to all 9 launcher locales.

## Approaches

- **Tabs:** Material3 `TabRow` inside the existing Settings card (chosen — minimal, no navigation
  graph, no change to back/top-bar semantics). Alternative: three `LauncherScreen` entries —
  rejected because it changes top bar and Back behavior. Alternative: just section headers —
  rejected because the card stays too long.
- **Component groups:** accordion header with group name + currently active name + chevron; one
  group expanded at a time to save portrait height (chosen). Alternative: `ExposedDropdownMenuBox`
  — rejected because checkbox rows with two lines of text do not fit a dropdown menu well.
- **Per-group actions:** one bottom **Импорт файла** button (chosen, per user clarification).
  Download stays as a compact text link under the downloadable v16 row (left column, not right);
  reset stays as a compact link in the group header and only when the slot has an override. This
  preserves all functions without putting buttons beside the names.

## Details left to the plan

- Short tab titles that fit portrait, new group/action strings in 9 locales.
- Keep "Активные компоненты" summary in the Graphics tab under the groups, computed from the
  hashes already loaded in IO.
- Verify: focused unit tests, `assembleModernDebug`, and one APK copied to the usual test path.
