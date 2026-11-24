# Settings Tabs and Component Groups Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the single long Settings card with three internal tabs and turn the component list into one expandable group per slot with a single import button.

**Architecture:** A small pure helper computes which GPU-driver slot is relevant; the existing SETTINGS branch of `WoWForeverScreen` renders a Material3 `TabRow` and moves its existing sections into one of three tab branches without changing their behavior. The component section becomes a single-expanded accordion whose rows contain only the checkbox and labels; all actions move to the group header (reset) or group footer (import/download).

**Tech Stack:** Kotlin, Jetpack Compose Material3, JUnit4.

**Spec:** `docs/superpowers/specs/2026-11-24-settings-tabs-component-groups-design.md`

## Global Constraints

- Version stays `0.2.0` (`versionCode = 3`); `assembleModernDebug` is the routine gate; no release build.
- Do not modify `tools/`, keystores, workflows, `gradle.properties`, `settings.gradle.kts`; no new dependencies.
- No secrets, personal data, binaries or device serials in git.
- Never auto-launch the game; gamepad A/B handling stays launcher-only and unchanged.
- Keep `ScreenTopBar`, system `BackHandler`, bottom back button and measured portrait centering intact.
- All new strings are added to all 9 `values*/strings_launcher.xml` locales.

## Review Focus

- Known Adreno 740 must show exactly one driver group (`turnip740`) — pinned by `driverSlotsFor` test in Task 1.
- Known Adreno 8xx must show exactly one driver group (`turnip8xx`) — pinned by `driverSlotsFor` test in Task 1.
- Unknown GPU must show both driver groups with the existing warning — pinned by `driverSlotsFor` test in Task 1 and the existing warning branch in Task 3.
- No override must show the built-in checkbox checked and no reset action — pinned by existing `ComponentCatalogTest` selection test, reused in Task 3.
- Portrait narrow width must not squeeze labels: no action buttons right of a component row — verified by `adb` screenshot in Task 4.

---

### Task 1: Driver grouping helper and localized tab strings

**Files:**
- Create: `app/src/main/java/app/gamenative/ui/screen/wow/SettingsComponentGroups.kt`
- Create: `app/src/test/java/app/gamenative/ui/screen/wow/SettingsComponentGroupsTest.kt`
- Modify: all 9 `app/src/main/res/values*/strings_launcher.xml`

**Interfaces:**
- Produces: `internal fun driverSlotsFor(driver: String): List<String>` returning `listOf("turnip740")`, `listOf("turnip8xx")`, or both; `internal fun componentGroupSlots(driver: String): List<String>` returning `listOf("proton", "dxvk") + driverSlotsFor(driver)`.
- Consumes: `DeviceCompatibility.DRIVER_740` and `DRIVER_8XX` from `app.gamenative.utils.DeviceCompatibility`.

- [ ] **Step 1: Write the failing test**

`SettingsComponentGroupsTest.kt` (JUnit4, plain `org.junit.Assert`):

```kotlin
@Test fun `740 driver shows only 740 slot`() {
    assertEquals(listOf("turnip740"), driverSlotsFor(DeviceCompatibility.DRIVER_740))
}
@Test fun `8xx driver shows only 8xx slot`() {
    assertEquals(listOf("turnip8xx"), driverSlotsFor(DeviceCompatibility.DRIVER_8XX))
}
@Test fun `unknown driver shows both slots`() {
    assertEquals(listOf("turnip740", "turnip8xx"), driverSlotsFor("unknown"))
}
@Test fun `groups always put proton and dxvk first`() {
    assertEquals(
        listOf("proton", "dxvk", "turnip8xx"),
        componentGroupSlots(DeviceCompatibility.DRIVER_8XX),
    )
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.SettingsComponentGroupsTest" --console=plain`
Expected: FAIL, unresolved reference `driverSlotsFor`.

- [ ] **Step 3: Implement the helpers in `SettingsComponentGroups.kt`**

Match `driver` with `equals(..., ignoreCase = true)` against the two `DeviceCompatibility` constants; unknown values return both slots in that order. `componentGroupSlots` prefixes `proton` and `dxvk`.

- [ ] **Step 4: Add the five new strings to all 9 locales**

| name | EN | RU | DE | ES | FR | JA | KO | zh-CN | pt-BR |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `wow_tab_launcher` | Launcher | Лаунчер | Launcher | Lanzador | Lanceur | ランチャー | 런처 | 启动器 | Iniciador |
| `wow_tab_game` | Game | Игра | Spiel | Juego | Jeu | ゲーム | 게임 | 游戏 | Jogo |
| `wow_tab_graphics` | Graphics | Графика | Grafik | Gráficos | Graphiques | グラフィック | 그래픽 | 图形 | Gráficos |
| `wow_components_group_proton` | Proton | Proton | Proton | Proton | Proton | Proton | Proton | Proton | Proton |
| `wow_components_group_dxvk` | DXVK | DXVK | DXVK | DXVK | DXVK | DXVK | DXVK | DXVK | DXVK |

- [ ] **Step 5: Run tests and build**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.SettingsComponentGroupsTest" --console=plain`
Expected: PASS, 4 tests.
Run: `./gradlew assembleModernDebug --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

`feat(ui): add settings tab strings and driver grouping helper`

---

### Task 2: Three tabs in the Settings screen

**Files:** Modify `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt` (SETTINGS branch, around lines 722–1043).

**Interfaces:**
- Consumes: `componentGroupSlots` is NOT used here; `wow_tab_launcher`, `wow_tab_game`, `wow_tab_graphics` from Task 1.
- Produces: nothing used by later tasks except the tab state kept local to this composable.

- [ ] **Step 1: Add tab enum and state**

Declare `private enum class SettingsTab { LAUNCHER, GAME, GRAPHICS }` near `LauncherScreen`, and `var settingsTab by remember { mutableStateOf(SettingsTab.LAUNCHER) }` next to the other local state.

- [ ] **Step 2: Insert the TabRow and split the sections**

Inside the existing Settings Card Column, keep `ScreenTopBar` first, insert a `TabRow(selectedTabIndex = settingsTab.ordinal)` with three `Tab`s using the localized strings from Task 1, then add `when (settingsTab)` with three branches, each a `Column(verticalArrangement = Arrangement.spacedBy(10.dp))`:

- `LAUNCHER` → the existing **Files** section and the existing **Updates** section (version text, client/application update buttons, skip, progress).
- `GAME` → the existing **Display** section (resolution, language) and **Account** section (login, forget login).
- `GRAPHICS` → the existing **LSFG** section; the component section is placed here by Task 3.

Keep `GamepadHint()` and `BottomBackButton(goToMain)` outside the `when` so they remain on every tab. Preserve every section's existing enabled conditions and callbacks verbatim.

- [ ] **Step 3: Verify build and existing tests**

Run: `./gradlew assembleModernDebug testModernDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL, no new failures.

- [ ] **Step 4: Commit**

`feat(ui): split settings into launcher, game and graphics tabs`

---

### Task 3: Expandable component groups with one import button

**Files:** Modify `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt` (component section in GRAPHICS branch, and `ComponentPickerRow` around line 1803).

**Interfaces:**
- Consumes: `componentGroupSlots(gpu.driver)` from Task 1; `activeComponentHashes`, `componentBusy`, `downloadComponent`, `resetComponent`, `selectComponent`, `componentPicker`, `pendingComponentId`, `componentRevision`, `refreshComponents()` from the existing composable.
- Produces: `ComponentPickerRow(label: String, secondary: String?, selected: Boolean, busy: Boolean, onCheckedChange: (Boolean) -> Unit, onDownload: (() -> Unit)? = null)` — when `onDownload` is non-null, a compact text link with `wow_components_download` is shown under the secondary line.

- [ ] **Step 1: Add expanded-group state**

`var expandedComponentSlot by remember { mutableStateOf<String?>(null) }`.

- [ ] **Step 2: Change `ComponentPickerRow` signature**

Remove `canDownload`, `canReset`, `onImport`, `onReset` parameters and the three `LinkButton`s on the right. Add the optional `onDownload` callback rendered as a compact `LinkButton`/text link in the left `Column` below `secondary`. Keep the checkbox, label, busy text and `TextOverflow.Ellipsis` unchanged.

- [ ] **Step 3: Rebuild the component section as an accordion**

For each `slot` in `componentGroupSlots(gpu.driver)`:
- Header: localized group label (`wow_components_group_proton`, `wow_components_group_dxvk`, or the recommended catalog `displayName` for a Turnip slot), active name from `activeHashes[slot]` using the existing bundled/catalog/external wording, a chevron icon, and — only when `activeHashes[slot] != null` — a compact reset link placed in a sibling `Row` (not inside the clickable header). Toggling sets `expandedComponentSlot` to `slot` or `null`.
- Body (only when expanded): the existing `ComponentStore.catalog.filter { it.slot == slot }` rows via `ComponentPickerRow`, followed by the existing unmatched-hash "custom file" row, then exactly **one** full-width `OutlinedButton` with `wow_components_import` that sets `pendingComponentId = slot` and launches `componentPicker`.
- Keep the existing unknown-GPU warning and `wow_components_unvalidated` text. Keep the `wow_components_slots_title` summary below all groups, unchanged in content.

- [ ] **Step 4: Verify build and tests**

Run: `./gradlew assembleModernDebug testModernDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL; 411+ tests, 0 failures (existing 407 plus Task 1's four).

- [ ] **Step 5: Commit**

`feat(ui): group components into expandable lists with one import button`

---

### Task 4: Verification and release notes

**Files:** Modify `docs/releases/v0.2.0.md`.

- [ ] **Step 1: Note the new Settings structure in release notes**

One line: Settings now has Launcher / Game / Graphics tabs; component groups are expandable with a single import button per group; built-in Proton remains recommended.

- [ ] **Step 2: Build one test APK**

Run: `./gradlew assembleModernDebug --rerun-tasks --console=plain`, then copy `app/build/outputs/apk/modern/debug/app-modern-debug.apk` to `test-apks/Classic-Forever-v0.2.0.apk` and to `C:\Users\legustoss\Documents\VSCode\classic-forever-android\test-apks\Classic-Forever-v0.2.0.apk`.

Expected: one APK, versionName `0.2.0`, versionCode `3`, bundled Proton SHA-256 `137807f4145b1b440075f83b2b1b3501d19c4eb618784b1a28418652cae3db03`.

- [ ] **Step 3: Device check**

Install the APK and capture a portrait screenshot of Settings → Graphics with a group expanded. Expected: no text squeezed by right-side buttons, one driver group, one import button at the bottom of the expanded group, all three tabs reachable, back button works.

- [ ] **Step 4: Commit**

`docs: record settings tab layout and verify v0.2.0 APK`
