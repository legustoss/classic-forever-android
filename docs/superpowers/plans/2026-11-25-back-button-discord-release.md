# Back Button, Discord Icon and Release Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show exactly one back control per orientation, use a real Discord logo, integrate the branch and produce the v0.2.0 release artifacts.

**Architecture:** A pure `backPlacementFor(compact)` helper maps window compactness to TOP or BOTTOM; `ScreenTopBar` gains a `showBack` flag and `BottomBackButton` drops `fillMaxWidth`. A local vector drawable replaces `Icons.Default.Forum`. Integration is a fast-forward merge into `main` from the repo's primary checkout, followed by a release build and tag.

**Tech Stack:** Kotlin, Jetpack Compose Material3, Android vector drawable, JUnit4, Gradle.

**Spec:** `docs/superpowers/specs/2026-11-25-back-button-discord-release-design.md`

## Global Constraints

- Version stays `0.2.0` (`versionCode = 3`); `assembleModernDebug` + unit tests are the routine gate.
- No new dependencies; no `tools/`, keystore, workflow or gradle changes; no secrets/personal data in git.
- `AGENTS.md` stays untracked; no game input or auto-launch changes.
- GitHub Release page cannot be created here: no `gh` CLI and no token — tag + APK are the deliverable.
- User approved merge to `main` and `v0.2.0` tag on 2026-11-25.

## Review Focus

- Compact portrait must show only the bottom Back control — pinned by `backPlacementFor` test in Task 1.
- Non-compact landscape must show only the top Back control — pinned by `backPlacementFor` test in Task 1.
- Bottom Back button must not span the screen width — verified by code inspection (`widthIn(min = 140.dp)`, no `fillMaxWidth`) in Task 1.
- Discord button must show the real Discord logo, not a generic forum icon — verified by drawable resource + `painterResource` usage in Task 2.
- Release APK must be version 0.2.0 / code 3 and the tag must point at the merged commit — verified in Task 3.

---

### Task 1: One Back control per orientation

**Files:**
- Create: `app/src/main/java/app/gamenative/ui/screen/wow/BackButtonPlacement.kt`
- Create: `app/src/test/java/app/gamenative/ui/screen/wow/BackButtonPlacementTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt`

**Interfaces:**
- Produces: `internal enum class BackPlacement { TOP, BOTTOM }` and `internal fun backPlacementFor(compact: Boolean): BackPlacement`.
- Consumes: existing `wide` boolean in `WoWForeverScreen` (`windowSizeClass.widthSizeClass != Compact`).

- [ ] **Step 1: Write the failing test**

`BackButtonPlacementTest.kt`:

```kotlin
@Test fun `compact shows bottom back`() {
    assertEquals(BackPlacement.BOTTOM, backPlacementFor(true))
}
@Test fun `non compact shows top back`() {
    assertEquals(BackPlacement.TOP, backPlacementFor(false))
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.BackButtonPlacementTest" --console=plain`
Expected: FAIL, unresolved reference `BackPlacement`.

- [ ] **Step 3: Implement the helper**

`backPlacementFor(true) = BOTTOM`, `false = TOP`.

- [ ] **Step 4: Wire it into the UI**

- `ScreenTopBar(title: String, onBack: () -> Unit, showBack: Boolean = true)`: when `showBack` is false, render only `SectionTitle` (no clickable row, no button); when true, keep the existing clickable row + `OutlinedButton` with `widthIn(min = 140.dp)`.
- `BottomBackButton`: replace `fillMaxWidth()` with `widthIn(min = 140.dp)`; keep height, shape, colors and gamepad support.
- At both `SETTINGS` and `FIRST_INSTALL` call sites: `ScreenTopBar(..., showBack = backPlacementFor(!wide) == BackPlacement.TOP)` and `if (backPlacementFor(!wide) == BackPlacement.BOTTOM) BottomBackButton(goToMain)`.

- [ ] **Step 5: Verify**

Run: `./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.screen.wow.BackButtonPlacementTest" --console=plain` → 2 tests pass.
Run: `./gradlew assembleModernDebug testModernDebugUnitTest --console=plain` → BUILD SUCCESSFUL, 420 tests, 0 failures, 12 skipped.

- [ ] **Step 6: Commit**

`feat(ui): keep one back control per orientation`

---

### Task 2: Real Discord icon

**Files:**
- Create: `app/src/main/res/drawable/ic_discord.xml`
- Modify: `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt` (community buttons around line 1762)

**Interfaces:** Produces `R.drawable.ic_discord`, consumed only by the Discord community button.

- [ ] **Step 1: Add the vector drawable**

`ic_discord.xml`: 24×24 viewport, fill `#FFFFFF`, pathData from the simple-icons Discord logo (`M20.317 4.3698 ... 2.4189Z`, full path). It will be tinted by the existing `WowNeon` content color.

- [ ] **Step 2: Replace the icon usage**

Swap `Icon(Icons.Default.Forum, ...)` for `Icon(painterResource(R.drawable.ic_discord), ...)` with the same `contentDescription` and `Modifier.size(20.dp)`. Remove the now-unused `Icons.Default.Forum` import if nothing else uses it.

- [ ] **Step 3: Verify**

Run: `./gradlew assembleModernDebug --console=plain` → BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

`feat(ui): use real Discord logo in community row`

---

### Task 3: Integrate and release v0.2.0

**Files:** no source changes; docs/release artifacts only.

- [ ] **Step 1: Final verification on the feature tree**

Run: `./gradlew assembleModernDebug testModernDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL, 420 tests, 0 failures, 12 skipped.

- [ ] **Step 2: Push the feature branch**

Run: `git push origin HEAD:refs/heads/feature/settings-tabs-component-groups`
Expected: branch updated to final HEAD.

- [ ] **Step 3: Merge into main in the primary checkout**

Primary checkout: `C:\Users\legustoss\Documents\VSCode\classic-forever-android`.
Run there: `git checkout main`, `git pull --ff-only`, `git merge --ff-only feature/settings-tabs-component-groups`.
If the merge is not fast-forward, stop and report rather than forcing.

- [ ] **Step 4: Build the release APK**

Run in the primary checkout: `./gradlew assembleModernRelease --console=plain`.
Copy `app/build/outputs/apk/modern/release/app-modern-release.apk` to `test-apks/Classic-Forever-v0.2.0-release.apk`. Confirm versionName `0.2.0` / versionCode `3` from BuildConfig.

- [ ] **Step 5: Tag and push**

Run in the primary checkout: `git tag -a v0.2.0 -m "v0.2.0"`, `git push origin main`, `git push origin v0.2.0`.
Report that GitHub Release page creation requires `gh`/token and was not performed.

- [ ] **Step 6: Report**

Give the user the release APK path, tag and commit; note the device check still needs their confirmation.
