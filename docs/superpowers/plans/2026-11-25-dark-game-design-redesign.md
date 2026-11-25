# Dark Game Design Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax for tracking. After plan approval: using-git-worktrees → subagent-driven-development, with a separate requesting-code-review agent after each task.

**Goal:** Ship a debug-only test build with a token-driven dark neon UI on launcher Main, Settings and Quick Menu, without touching business logic or native code.

**Architecture:** Tokens and custom Compose primitives live in a new `app.gamenative.ui.gameui` package. `WoWForeverScreen` and `XServerScreen` keep all state and callbacks and only select the new presentational tree when `BuildConfig.DEBUG` is true. Release remains unchanged.

**Tech Stack:** Kotlin, Jetpack Compose foundation, WindowSizeClass, JUnit4, Android vector/font resources.

**Spec:** `docs/superpowers/specs/2026-11-25-dark-game-design-redesign-design.md`

## Global Constraints

- Version stays `0.2.0`; release and published tag are untouched; only `assembleModernDebug` is built.
- New code must not import `androidx.compose.material3.*` or call `MaterialTheme`.
- No changes to C/C++/JNI/NDK, GLSL, shaders, business logic, API or DB schemas.
- No new product flavor and no new dependencies besides downloaded font files.
- Fonts come from Google Fonts (OFL) into `app/src/main/res/font/`.
- All interactive elements need `contentDescription`/semantics and 48dp touch targets; text contrast ≥ 4.5:1.
- Respect `ANIMATOR_DURATION_SCALE == 0`; device address and serial must never reach git.
- Use the named subagents only; do not select models. `systems-programming__cpp-pro` is out of scope.

## Review Focus

- Debug switch must not affect release behavior — verified by inspecting the `BuildConfig.DEBUG` branches in Task 5.
- No material3 import in any new file — verified by grep in each task's review.
- Only one glowing element per screen — pinned by `GlowButton` active-state test in Task 1.
- Text contrast ≥ 4.5:1 — pinned by token contrast test in Task 1.
- Touch targets ≥ 48dp — pinned by GlowButton geometry test in Task 1.
- TalkBack exposure of interactive controls — verified by semantics/uiautomator dump in Task 5.

---

### Task 1: Tokens, theme, fonts and primitives

**Agent:** `ui-design__design-system-architect`
**Files:**
- Create: `app/src/main/java/app/gamenative/ui/gameui/theme/GameTheme.kt`
- Create: `app/src/main/java/app/gamenative/ui/gameui/components/{NeonPanel,GlowButton,HudLabel,GameToggle,GameCheckbox,GameDropdown,GameBackdrop}.kt`
- Create: `app/src/main/res/font/space_grotesk_{regular,medium,semibold,bold}.ttf`, `jetbrains_mono_{regular,medium}.ttf`
- Test: `app/src/test/java/app/gamenative/ui/gameui/GameTokensTest.kt`, `GameComponentsTest.kt`

**Interfaces:**
- Produces: `GameTheme`, `GameTheme.tokens/colors/type`; `NeonPanel(active: Boolean = false, content: @Composable () -> Unit)`; `GlowButton(text: String, onClick: () -> Unit, enabled: Boolean = true, active: Boolean = false, modifier: Modifier = Modifier)`; `HudLabel(label: String, value: String, modifier: Modifier = Modifier)`; `GameBackdrop(content: @Composable () -> Unit)` drawing one quiet Canvas grid in token `stroke` color behind content, no shader; `GameToggle`, `GameCheckbox`, `GameDropdown` with the same callback shapes as the material3 widgets they replace; `rememberReducedMotion(): Boolean`; `motionDuration(scale: Float, baseMs: Int): Long`.
- Consumes: the two files at `C:\Users\legustoss\Documents\Проект по умолчанию\design-system`.

- [ ] **Step 1: Write failing tests**

`GameTokensTest`: `text and muted contrast against background >= 4.5`, `primary and signal are the only accents`, `motionDuration(0f, 220) == 0L`, `motionDuration(1f, 220) == 220L`.
`GameComponentsTest`: `glow follows active not enabled`, `disabled button maps to muted color`, `min touch target is 48dp`.

- [ ] **Step 2: Run tests, expect RED**

`./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.gameui.*" --console=plain` → FAIL, unresolved references.

- [ ] **Step 3: Implement theme and primitives**

Copy tokens verbatim from `DESIGN_TOKENS.md`; adapt `GameTheme.kt` package to `app.gamenative.ui.gameui.theme`; wire downloaded fonts with `Font(R.font...)`; add `active` to `GlowButton`, `Role.Button` semantics, `defaultMinSize(48.dp, 48.dp)`; add `GameBackdrop`; implement the pure motion helpers. No material3 imports, no GLSL/native changes.

- [ ] **Step 4: Verify GREEN**

`./gradlew testModernDebugUnitTest --tests "app.gamenative.ui.gameui.*" --console=plain` → PASS.
`./gradlew assembleModernDebug --console=plain` → BUILD SUCCESSFUL.
Grep: no `material3` import in `app/src/main/java/app/gamenative/ui/gameui/`.

- [ ] **Step 5: Review + commit**

Separate reviewer agent using `requesting-code-review/code-reviewer.md`; then commit `feat(design): add game tokens, fonts and primitives`.

---

### Task 2: Launcher Main screen

**Agent:** `ui-design__ui-designer`
**Files:** Create `app/src/main/java/app/gamenative/ui/gameui/screens/GameLauncherMain.kt`, `app/src/main/java/app/gamenative/ui/gameui/previews/GameLauncherMainPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/gameui/GameLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 `GameTheme`, `NeonPanel`, `GlowButton`, `HudLabel`, `GameToggle`.
- Produces: `GameLauncherMain(...)` with parameters equal to the values/callbacks already read in the `MAIN` branch of `WoWForeverScreen.kt` (files status, version status, resolution, login state, server status, gamepad state, `onLaunch`, `onOpenSettings`, `onOpenFirstInstall`, `onRefresh`, `onFolderPick`, `onAddonPick`, `onLogin`, `onForgetUser`, `onToggleLsfg`, `onImportLsfg`); `gameLayoutFor(width: WindowWidthSizeClass): GameLayout` with `SINGLE` for Compact and `TWO_COLUMN` otherwise.

- [ ] **Step 1: Failing test** — compact → `SINGLE`, medium/expanded → `TWO_COLUMN`.
- [ ] **Step 2: Run focused test, expect RED.**
- [ ] **Step 3: Implement the presentational screen and the helper.** Use `GameBackdrop` and one 220 ms entrance animation via `motionDuration`; no state of its own, no material3 imports, no auto-launch.
- [ ] **Step 4: Add @Preview for compact, medium and expanded widths; run tests + `assembleModernDebug`.**
- [ ] **Step 5: Review + commit** `feat(design): add game launcher main screen`.

---

### Task 3: Settings screen

**Agent:** `framework-migration__legacy-modernizer`
**Files:** Create `app/src/main/java/app/gamenative/ui/gameui/screens/GameSettingsScreen.kt`, `app/src/main/java/app/gamenative/ui/gameui/previews/GameSettingsPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/gameui/GameSettingsLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 primitives including `GameToggle`, `GameCheckbox`, `GameDropdown`.
- Produces: `GameSettingsScreen(...)` with parameters equal to the state/callbacks of the `SETTINGS` branch; `settingsColumnsFor(width: WindowWidthSizeClass): Int` returning 1 for Compact, 2 otherwise.

- [ ] **Step 1: Failing test** for `settingsColumnsFor`.
- [ ] **Step 2: Run focused test, expect RED.**
- [ ] **Step 3: Implement the three tabs (Launcher / Game / Graphics) with custom controls; keep callbacks identical; login/update dialogs stay legacy overlays.** Use `GameBackdrop` and the reduced-motion-aware entrance; no material3 imports.
- [ ] **Step 4: Add @Preview for three widths; run tests + `assembleModernDebug`.**
- [ ] **Step 5: Review + commit** `feat(design): add game settings screen`.

---

### Task 4: Quick Menu screen

**Agent:** `framework-migration__legacy-modernizer`
**Files:** Create `app/src/main/java/app/gamenative/ui/gameui/screens/GameQuickMenu.kt`, `app/src/main/java/app/gamenative/ui/gameui/previews/GameQuickMenuPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/gameui/GameQuickMenuLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 primitives.
- Produces: `GameQuickMenu(...)` with the same parameter list as legacy `QuickMenu(...)`; `quickMenuColumnsFor(width: WindowWidthSizeClass): Int` returning 1 for Compact, 2 otherwise.

- [ ] **Step 1: Failing test** for `quickMenuColumnsFor`.
- [ ] **Step 2: Run focused test, expect RED.**
- [ ] **Step 3: Restyle shell, tabs and action rows; keep every `QuickMenuAction` handler and state unchanged.** Use `GameBackdrop` and the reduced-motion-aware entrance; no material3 imports, no game input changes.
- [ ] **Step 4: Add @Preview for three widths; run tests + `assembleModernDebug`.**
- [ ] **Step 5: Review + commit** `feat(design): add game quick menu screen`.

---

### Task 5: Integration, device verification and report

**Agent:** controller with `agent-teams__team-lead` coordination
**Files:** Modify `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt`, `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`

- [ ] **Step 1: Add debug-only selection**

In `WoWForeverScreen`, `val useGameDesign = BuildConfig.DEBUG`; in `MAIN` and `SETTINGS` branches, wrap `GameTheme { GameLauncherMain(...) }` / `GameTheme { GameSettingsScreen(...) }` when true, legacy otherwise. In `XServerScreen`, at the single `QuickMenu(...)` call site, use `GameTheme { GameQuickMenu(...) }` in debug. Keep every callback identical.

- [ ] **Step 2: Verify no regressions**

`./gradlew testModernDebugUnitTest --console=plain` → all green.
`./gradlew assembleModernDebug --console=plain` → BUILD SUCCESSFUL.
Grep: no new material3 import in `gameui/`; no native/GLSL file touched (`git diff --stat`).

- [ ] **Step 3: Device verification over Wi‑Fi**

`adb connect 10.0.1.92:5555`; install APK; open launcher; capture portrait screenshot; `adb shell uiautomator dump` for semantics; `adb logcat` filtered for crashes. Do not commit the address or screenshots containing personal data.

- [ ] **Step 4: Final review and report**

Separate reviewer agent for the whole branch; fix Critical/Important with RED→GREEN tests. Report changed files, commands and outputs, APK path, screenshots/previews, and limitations. Commit `feat(design): enable game UI in debug build`.

---

## Execution notes

- Worktree: create with `using-git-worktrees` from `main` after plan approval; branch `feature/dark-game-design`.
- Tasks 2–4 are independent after Task 1; dispatch them in parallel with disjoint file ownership.
- Each task gets a fresh `requesting-code-review` reviewer before its commit, plus one whole-branch review at the end.
- Use `systems-programming__cpp-pro` only if the user later approves a real GLSL background; not used here.
- `writing-skills` only if a new reusable pattern needs to be recorded; not anticipated.
