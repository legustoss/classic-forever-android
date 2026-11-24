# Launcher layout regression and ADB testing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore comfortable vertical placement of the launcher main screen, make the Settings/FirstInstall back control reliably tappable, and give the user a reproducible Windows ADB workflow for performance comparison.

**Architecture:** Keep the single scrollable launcher column and top-align tall screens so the top bar is never clipped; add a small deterministic top spacer only for the short portrait Main screen. Make the whole top bar row clickable for back, with the explicit button retained as a visual affordance. ADB guidance is documentation only.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit4, Git, Windows PowerShell, Android platform-tools.

**Spec:** `docs/SESSION-STATE.md` plus user report 2026-11-24: "in v16 test APK the layout moved upward, back button does not work, have to swipe to switch launcher screen"; request for ADB connection instructions for performance tests.

## Global Constraints

- Version stays 0.1.1; next release v0.2.0 is not prepared here.
- `./gradlew assembleModernDebug` is the only routine build gate; release builds only when explicitly preparing a release.
- Do not modify `tools/`, keystores, workflows, `gradle.properties`, `settings.gradle.kts`.
- No new dependencies, no binaries or personal data in git.
- Do not commit `AGENTS.md`; it is local-only and gitignored.
- Never auto-launch the game; launch only from launcher buttons.
- Layout must use `WindowSizeClass`; no `isTablet` booleans and no fixed-width buttons.
- Gamepad changes must not alter in-game input.
- No invented FPS numbers; performance results come from device measurement only.

## Review Focus

- Tall FirstInstall/Settings content: top bar must stay reachable — test: build plus manual device check; pure test for top-padding decision.
- Portrait Main short content: must not look glued to the top — test: pure `launcherTopPadding` test.
- Back control: top bar row and explicit button must both respond — test: code review of clickable modifier plus device check.
- Gamepad A/B handling must not fire on keyboard/touch — test: existing `ComponentCatalogTest` key-mapping test.
- ADB guide: commands must be copy-pasteable on Windows and use the real package name `app.wowforever` — test: `git diff --check` and doc review.

---

### Task 1: Restore launcher vertical rhythm and reliable back bar

**Files:**
- Modify: `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt`
- Create: `app/src/test/java/app/gamenative/ui/screen/wow/LauncherLayoutTest.kt`

**Interfaces:**
- Consumes: `LauncherLayout(fullBleed: Boolean, modifier: Modifier, content: @Composable BoxScope.() -> Unit)`, `ScreenTopBar(title: String, onBack: () -> Unit)`, `goToMain`.
- Produces: `internal fun launcherTopPadding(isMain: Boolean, isWide: Boolean): Dp`, used only by the portrait Main branch; `ScreenTopBar` row clickable for back.

- [ ] **Step 1: Write the failing test**

Create `LauncherLayoutTest.kt`:

```kotlin
package app.gamenative.ui.screen.wow

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherLayoutTest {
    @Test
    fun portrait_main_gets_top_spacer() {
        assertEquals(64.dp, launcherTopPadding(isMain = true, isWide = false))
    }

    @Test
    fun other_screens_have_no_extra_top_spacer() {
        assertEquals(0.dp, launcherTopPadding(isMain = false, isWide = false))
        assertEquals(0.dp, launcherTopPadding(isMain = true, isWide = true))
        assertEquals(0.dp, launcherTopPadding(isMain = false, isWide = true))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.LauncherLayoutTest" --console=plain`
Expected: FAIL with unresolved reference `launcherTopPadding`.

- [ ] **Step 3: Implement `launcherTopPadding` and apply it**

In `WoWForeverScreen.kt` add `import androidx.compose.ui.unit.Dp` if missing, then add:

```kotlin
internal fun launcherTopPadding(isMain: Boolean, isWide: Boolean): Dp =
    if (isMain && !isWide) 64.dp else 0.dp
```

In the portrait Main branch (not landscape), add `Spacer(Modifier.height(launcherTopPadding(true, false)))` as the first child. Keep `LauncherLayout` top-aligned so tall screens never clip the top bar.

- [ ] **Step 4: Make the back row reliably tappable**

In `ScreenTopBar`, add `Modifier.clickable(onClick = onBack)` to the `Row`; keep the existing `OutlinedButton` with `gamepadConfirm(onBack)` as the visible affordance. Add `androidx.compose.foundation.clickable` import if missing.

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.LauncherLayoutTest" --console=plain`
Expected: PASS, 2 tests.

- [ ] **Step 6: Build gate**

Run: `./gradlew assembleModernDebug --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt app/src/test/java/app/gamenative/ui/screen/wow/LauncherLayoutTest.kt
git commit -m "fix(ui): restore launcher rhythm and widen back target"
```

### Task 2: Document Windows ADB workflow for performance tests

**Files:**
- Create: `docs/adb-testing.md`

**Interfaces:**
- Consumes: package name `app.wowforever`, test APK paths in `test-apks/`, scenarios in `docs/benchmarks.md`.
- Produces: copy-pasteable Windows PowerShell instructions; no source API changes.

- [ ] **Step 1: Write the ADB guide**

Create `docs/adb-testing.md` containing: install platform-tools, enable Developer options and USB debugging on the device, accept the RSA prompt, verify with `adb devices`, install with `adb install -r test-apks\Classic-Forever-baseline.apk` (and the v16/stripped variants), clear app data before switching builds, collect logs with `adb logcat -s ClassicForever`, performance with `adb shell dumpsys gfxinfo app.wowforever`, Perfetto command pointer, and pulling logs from `/storage/emulated/0/WoW Forever/logs/`.

- [ ] **Step 2: Verify doc formatting**

Run: `git diff --check`
Expected: no whitespace errors.

- [ ] **Step 3: Commit**

```bash
git add docs/adb-testing.md
git commit -m "docs: add Windows ADB testing workflow"
```

### Task 3: Final verification and state snapshot

**Files:**
- Modify: `docs/SESSION-STATE.md`

**Interfaces:**
- Consumes: results of Task 1 and Task 2.
- Produces: updated snapshot with build/test evidence and pending device comparison.

- [ ] **Step 1: Run full verification**

```bash
./gradlew assembleModernDebug testModernDebugUnitTest --continue --console=plain
```
Expected: BUILD SUCCESSFUL; 397 tests (395 + 2), 0 failures, 12 skipped.

- [ ] **Step 2: Update state snapshot**

In `docs/SESSION-STATE.md`, record the new test count, the layout fix, the ADB guide, and that the user is currently comparing v16 on device.

- [ ] **Step 3: Commit**

```bash
git add docs/SESSION-STATE.md
git commit -m "docs: refresh session snapshot after layout and ADB work"
```
