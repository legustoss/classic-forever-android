# AMOLED Minimal Design Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax. After plan approval: new git worktree from `main`, per-task implementer + reviewer, TDD, whole-branch review, verification, finishing branch.

**Goal:** Replace the rejected neon design with a minimal AMOLED black/white UI with one muted-cyan accent and three bottom-anchored actions on the main screen.

**Architecture:** New `app.gamenative.ui.monotheme` package built from `main`, independent of the rejected `gameui` branch. Presentational screens receive existing state/callbacks; a `BuildConfig.DEBUG` switch selects them at the legacy call sites, leaving release untouched.

**Tech Stack:** Kotlin, Jetpack Compose foundation, WindowSizeClass, JUnit4.

**Spec:** `docs/superpowers/specs/2026-11-25-amoled-minimal-design-design.md`

## Global Constraints

- Version stays `0.2.0`; only `assembleModernDebug`; no release build, push or merge.
- Pure black `#000000`, text `#FFFFFF`, secondary `#A7B0BA`, border `#2A2A2A`, one accent `#7FE9DC`.
  No pink/signal colour; no decorative background grid.
- New code must not import material3 except the exempted `androidx.compose.material3.windowsizeclass.*`
  platform measurement APIs; no MaterialTheme/Material composables.
- No native/C/C++/JNI/GLSL/shader, business logic, API or DB changes; no new dependency/flavor/gradle change.
- All touch targets >= 48dp; labels stay on one line with ellipsis;TalkBack labels on the focusable node.
- Reduced motion: one 220 ms Main-screen entrance, zero at `ANIMATOR_DURATION_SCALE == 0`.
- Use existing `bricolage_grotesque_*` font resources and default monospace for HUD numbers; no new font download.
- Do not commit device address/serial or screenshots; artifacts stay in gitignored SDD workspace.

## Review Focus

- Four-line wrapped Settings labels must not recur — pinned by two-row layout helper test and device screenshot in Task 5.
- Website/Discord links and launch/update status must remain reachable — pinned by Task 2 callback test and Task 5 device check.
- TalkBack name must be on the focusable node, not a sibling — pinned by Task 1 semantics test and Task 5 `uiautomator dump`.
- Every interactive control in portrait bottom 40% — pinned by layout helper tests in Tasks 2–4 and device check.
- No second accent colour or decorative glow on passive panels — pinned by Task 1 token test.

---

### Task 1: MonoTheme tokens and primitives

**Agent:** `ui-design__design-system-architect`
**Files:** Create `app/src/main/java/app/gamenative/ui/monotheme/theme/MonoTheme.kt`, `.../components/{MonoPanel,MonoButton,MonoIconButton,MonoLabel,MonoProgress,MonoSheet}.kt`
**Test:** `app/src/test/java/app/gamenative/ui/monotheme/MonoTokensTest.kt`, `MonoSemanticsTest.kt`

**Interfaces:**
- Produces `MonoTheme`, `MonoTheme.colors`, `MonoButton(text, onClick, enabled, active, modifier)`,
  `MonoIconButton(icon, contentDescription, onClick, enabled)` (>=48dp), `MonoPanel`, `MonoLabel`,
  `MonoProgress(value: Float?, label: String)`, `MonoSheet(title, onDismiss, content)`;
  `motionDuration(scale: Float, baseMs: Int): Long`; `monoButtonLabel(text): String` pure helper.
- Consumes: spec palette values verbatim.

- [ ] **Step 1: failing tests** — palette hex values; secondary contrast >= 4.5:1; only one accent token;
  `motionDuration(0f, 220) == 0L`; `monoButtonLabel` returns a single-line-safe string for long labels.
- [ ] **Step 2: run focused tests, expect RED.**
- [ ] **Step 3: implement tokens and primitives.** `contentDescription` in the same `semantics` call as
  `clickable`/`Role`, before padding. No material3 imports. No glow on passive panels.
- [ ] **Step 4: GREEN focused + full suite + `assembleModernDebug`.**
- [ ] **Step 5: review + commit** `feat(amoled): add mono tokens and primitives`.

---

### Task 2: Main screen with three bottom actions

**Agent:** `ui-design__ui-designer`
**Files:** Create `.../screens/MonoMainScreen.kt`, `.../previews/MonoMainPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/monotheme/MonoMainLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 primitives.
- Produces `MonoMainScreen(...)` with the legacy Main state/callbacks; `monoMainLayout(width: WindowWidthSizeClass): MonoMainLayout`
  returning `BOTTOM_STACK` for Compact and `BOTTOM_RIGHT` otherwise; `moreSheetActions(hasLogin: Boolean): List<MonoAction>`
  returning `Сайт`, `Discord`, and `Вход` only when login is saved.

- [ ] **Step 1: failing tests** for `monoMainLayout` and `moreSheetActions`.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement.** Title + one status line + thin `MonoProgress` while launching/updating;
  bottom buttons Играть / Настройки / Ещё; `Ещё` opens `MonoSheet` with Сайт/Discord/Вход callbacks.
  Restore Website/Discord links and status text; no material3; one 220 ms entrance via `motionDuration`.
- [ ] **Step 4: previews Compact/Medium/Expanded; tests + build.**
- [ ] **Step 5: review + commit** `feat(amoled): add minimal main screen`.

---

### Task 3: Settings with bottom tabs

**Agent:** `framework-migration__legacy-modernizer`
**Files:** Create `.../screens/MonoSettingsScreen.kt`, `.../previews/MonoSettingsPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/monotheme/MonoSettingsLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 primitives, legacy `sectionsFor`, `componentGroupSlots`.
- Produces `MonoSettingsScreen(...)` with all legacy Settings state/callbacks; `settingsActionRows(count: Int): Int`
  returning 2 for four actions; `labelFitsLine(text: String, minWidthDp: Int): String` pure helper that returns
  an ellipsised single-line string (no letter-by-letter wrap).

- [ ] **Step 1: failing tests** for both helpers, including a long Russian label.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement three tabs at the bottom with compact back; content scrolls above.**
  Actions in two rows with min widths; component groups expandable with one import button each.
  Legacy dialogs remain overlays; no material3 in new code.
- [ ] **Step 4: previews + tests + build.**
- [ ] **Step 5: review + commit** `feat(amoled): add settings with bottom tabs`.

---

### Task 4: Quick Menu minimal

**Agent:** `framework-migration__legacy-modernizer`
**Files:** Create `.../screens/MonoQuickMenu.kt`, `.../previews/MonoQuickMenuPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/monotheme/MonoQuickMenuLayoutTest.kt`

**Interfaces:**
- Consumes: Task 1 primitives.
- Produces `MonoQuickMenu(...)` with the legacy 21-parameter list; `quickMenuActionRows(count: Int): Int`
  returning ceil(count/2) capped at 3; all `QuickMenuAction` handlers preserved.

- [ ] **Step 1: failing tests** for row counting and single-line labels.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement shell/tabs/action rows with 48dp+ targets and single-line labels.**
  No material3 in new code; keep all handlers; no game input changes.
- [ ] **Step 4: previews + tests + build.**
- [ ] **Step 5: review + commit** `feat(amoled): add minimal quick menu`.

---

### Task 5: Integration and device verification

**Agent:** `agent-teams__team-lead`
**Files:** Modify `WoWForeverScreen.kt`, `XServerScreen.kt` debug call sites only.

- [ ] **Step 1: add `val useMonoDesign = BuildConfig.DEBUG` and route MAIN/SETTINGS/QuickMenu to the new screens;**
  keep all callbacks including Website/Discord, status text, progress flags, account login.
- [ ] **Step 2: verify** `./gradlew testModernDebugUnitTest assembleModernDebug --rerun-tasks`, grep no
  forbidden material3 imports, `git diff --check`, no native/GLSL changes.
- [ ] **Step 3: device check over Wi-Fi ADB** — launch app, screenshot portrait Main and Settings,
  `uiautomator dump` to confirm focusable nodes carry `content-desc`, `logcat` for crashes, check labels
  do not wrap and buttons are bottom-reachable. Put artifacts in gitignored SDD workspace.
- [ ] **Step 4: whole-branch review, fix any Critical/Important with RED→GREEN tests, then report**
  changed files, commands+outputs, APK path, and limitations. Commit `feat(amoled): enable mono UI in debug build`.

---

## Execution notes

- New worktree `feature/amoled-minimal-design` from `main` after approval; rejected `feature/dark-game-design`
  branch stays untouched and is not merged.
- Tasks 2–4 are independent after Task 1; dispatch sequentially per SDD skill, each with its own reviewer.
- Model for all subagents: `opencode/hy4-flash-free` as configured by the user.
