# Figma Launcher Design Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax. After plan approval: create a new worktree from `main`, per-task implementer + reviewer, TDD, whole-branch review, verification, finishing branch.

**Goal:** Reproduce the five user-supplied Figma launcher screens exactly in a debug build, with real runtime values and equal phone/tablet treatment.

**Architecture:** New `app.gamenative.ui.figma` package built from `main`, independent of the previous `monotheme` branch. Presentational screens take existing state/callbacks; a `BuildConfig.DEBUG` switch selects them at launcher call sites while release stays untouched.

**Tech Stack:** Kotlin, Jetpack Compose foundation, WindowSizeClass, Cinzel/Cinzel Decorative font resources, JUnit4.

**Spec:** `DESIGN.md` (rewritten 2026-11-26), with the five user images as the visual authority.

## Global Constraints

- Version stays `0.2.0`; only `assembleModernDebug`; no release build, push or merge until asked.
- New code must not import material3 except exempted `androidx.compose.material3.windowsizeclass.*` platform APIs; no MaterialTheme/Material composables.
- No native/C/C++/JNI/GLSL/shader, business logic, API or DB changes; no new dependency/flavor/gradle change.
- Palette exactly `#000000`, `#FFFFFF`, `#8A8A8A`, `#3A3A3A`; no accent colour, icons, gradients or shadows.
- Fonts: **Cinzel** for all text, **Cinzel Decorative** for headings. No second family.
- Real values only: `BuildConfig.VERSION_NAME` (0.2.0), real client build, detected driver/resolution/render scale. Never display the mock's 0.3.0, 100%, Adreno 840 or x2 unless those are the actual values.
- Touch targets >= 48dp, TalkBack names on focusable nodes, system Back works, reduced motion respected.
- No device access without explicit user permission; verification uses tests, previews and static checks.

## Review Focus

- Five Figma screens matched in composition, type, label case and outline geometry.
- Real values replace mock numbers without breaking the layout.
- Release path untouched; debug-only switch at existing call sites.
- Phone and tablet/foldable both supported; no letter-by-letter wrapping or clipped controls.
- TalkBack names and 48dp targets on every control; no invented data.

---

### Task 1: Theme, fonts and monochrome primitives

**Agent:** `ui-design__design-system-architect`
**Files:** Create `app/src/main/java/app/gamenative/ui/figma/theme/FigmaTheme.kt`, `.../components/{FigmaButton,FigmaField,FigmaTab,FigmaRow,FigmaVersion}.kt`, `app/src/main/res/font/cinzel_*.ttf`, `cinzel_decorative_*.ttf`
**Test:** `app/src/test/java/app/gamenative/ui/figma/FigmaTokensTest.kt`

**Interfaces:**
- Produces `FigmaTheme`, `FigmaTheme.colors/typography`, `FigmaButton(text, onClick, modifier, enabled)`, `FigmaField(label, value, onValueChange)`, `FigmaTab(text, active, onClick)`, `FigmaRow(label, actionLabel, onClick)`, `FigmaVersion(appVersion, clientVersion)`.
- Consumes: Cinzel font files from Google Fonts (OFL), placed in `res/font`.

- [ ] **Step 1: failing tests** — four palette values, no accent token, all type styles use Cinzel/Cinzel Decorative, outline radius 12dp and stroke 1dp.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement theme/primitives.** Text-only controls, no icons, no fills, no material3.
- [ ] **Step 4: GREEN focused + full suite + `assembleModernDebug`.**
- [ ] **Step 5: review + commit** `feat(figma): add monochrome serif theme and primitives`.

---

### Task 2: Login and Welcome screens

**Agent:** `ui-design__ui-designer`
**Files:** Create `.../screens/FigmaLoginScreen.kt`, `FigmaWelcomeScreen.kt`, `.../previews/FigmaEntryPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/figma/FigmaEntryTest.kt`

**Interfaces:**
- Consumes Task 1 primitives.
- Produces `FigmaLoginScreen(...)` and `FigmaWelcomeScreen(...)` with callbacks for login, create account, community site, launch, settings and every welcome action. All values passed in; no state machine.

- [ ] **Step 1: failing tests** for exact label set and order: `WORLD OF WARCRAFT / CLASSIC FOREVER`, `ACCOUNT NAME`, `PASSWORD`, `LOGIN`, `CREATE A ACCOUNT`, `COMMUNITY SITE`, `LAUNCH`, `SETTINGS`, version block, and welcome heading/numbered steps.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement to the screenshots.** Login: title top-left, centered form, lower-left/lower-right actions. Welcome: centered heading, right-aligned step actions, LAUNCH bottom-right.
- [ ] **Step 4: previews compact/medium/expanded; tests + build.**
- [ ] **Step 5: review + commit** `feat(figma): add login and welcome screens`.

---

### Task 3: Launcher, Game and Components settings

**Agent:** `framework-migration__legacy-modernizer`
**Files:** Create `.../screens/FigmaLauncherSettings.kt`, `FigmaGameSettings.kt`, `FigmaComponentsSettings.kt`, `.../previews/FigmaSettingsPreviews.kt`
**Test:** `app/src/test/java/app/gamenative/ui/figma/FigmaSettingsTest.kt`

**Interfaces:**
- Consumes Task 1 primitives and real values from caller.
- Produces three presentational screens with the same callbacks as legacy settings.

Screen contents exactly:
- **Launcher:** `LAUNCHER / GAME / COMPONENTS` sidebar; `APP` + `UPDATE`; `LAUNCHER THEME` + `COMING SOON…`; divider; `GAME` group; `GAME FOLDER` + `CHANGE`; `GAME CLIENT` + `DOWNLOAD`; `CLOSE`; version block.
- **Game:** same sidebar; heading `GAME`; `RESOLUTION` + real value; `RENDER SCALE` + real value; divider; `LANGUAGE` + real value; `ACCOUNT` + `RESET PASSWORD`; `CLOSE`.
- **Components:** heading `COMPATIBILITY`; `PROTON` + real version; `DXVK` + real version; `DRIVER` + detected value; divider; `LOSSLESS SCALING` + real state; `FRAME GENERATION` + real multiplier; `CLOSE`.

- [ ] **Step 1: failing tests** for exact label sets and ordering.
- [ ] **Step 2: RED.**
- [ ] **Step 3: implement with left sidebar, vertical hairline, label/action rows, bottom-right CLOSE and bottom-left version.** Real values only.
- [ ] **Step 4: previews compact/medium/expanded; tests + build.**
- [ ] **Step 5: review + commit** `feat(figma): add launcher, game and component settings`.

---

### Task 4: Debug integration and responsive shell

**Agent:** `agent-teams__team-lead`
**Files:** Modify `WoWForeverScreen.kt`, `XServerScreen.kt` debug call sites only; add previews if needed.

- [ ] **Step 1: add `val useFigmaDesign = BuildConfig.DEBUG` and route login, Main, Settings, FirstInstall and welcome states to the new screens; keep legacy `else` branches.**
- [ ] **Step 2: use a non-scrolling full-bleed shell for debug screens so bottom alignment works; FIRST_INSTALL stays on legacy shell if it is not redesigned.**
- [ ] **Step 3: verify `./gradlew testModernDebugUnitTest assembleModernDebug --console=plain`; grep no forbidden material3 imports; `git diff --check`; no native/GLSL/business changes.**
- [ ] **Step 4: review + commit** `feat(figma): enable Figma launcher in debug build`.

---

### Task 5: Verification and report

- [ ] **Step 1: fresh `--rerun-tasks` test + build.**
- [ ] **Step 2: APK copied to `test-apks/Classic-Forever-v0.2.0-figma-test.apk`.**
- [ ] **Step 3: whole-branch review; fix Critical/Important with RED→GREEN tests; report changed files, commands, APK path and limitations.**

No adb/device steps are included because the user has not authorised further device access.
