# Dark game design redesign — design

## Intent and why

Prepare a **test-only** build with a deliberate dark/gaming/neon visual identity. The public repository
already has a usable but generic near-black Material3 theme. The user wants a distinctive style
defined by tokens, without touching business logic, native code, APIs or the database schema.

## Goals

- Copy `DESIGN_TOKENS.md` and `GameTheme.kt` from `C:\Users\legustoss\Documents\Проект по умолчанию\design-system`
  into the app, replacing the placeholder package.
- Add Space Grotesk and JetBrains Mono as font resources (download from Google Fonts, OFL).
- Build a `GameTheme` that does **not** inherit `MaterialTheme` and is used without importing material3
  in new code.
- Add reusable `NeonPanel`, `GlowButton` and `HudLabel`. Glow appears on one active element at a time.
  The palette has one accent (`primary`) plus one signal (`signal`) used only for warnings/danger.
- Redesign three screens, chosen with the user: **launcher Main**, **Settings**, **Quick Menu**.
- Keep navigation and all behavior: same `NavHost` routes, same callbacks and state, no auto-launch change.
- Adaptive phone / tablet / foldable via `WindowSizeClass`, with `@Preview` for each configuration.
- Accessibility: content descriptions on interactive elements, 48dp touch targets, text contrast at
  least 4.5:1, and respect `ANIMATOR_DURATION_SCALE == 0`.
- Produce a debug APK through the existing `modernDebug` variant; leave release and its published v0.2.0
  untouched.

## Non-goals and anti-goals

- No changes to C/C++/JNI/NDK, GLSL logic, shaders, business logic, API or DB schemas.
- No new product flavor. Isolation comes from a separate git worktree and a debug-only switch.
- No cream/terracotta palette, acid green, identical cards with soft gray shadows, ALL-CAPS eyebrow
  labels, or arrows appended to button text. No fade-and-slide-up on every element.
- No Material3 widgets in new code. Existing untouched screens may keep `PluviaTheme` and Material3.

## Constraints

- Modules: single `app` module; flavor `modern` (dimension `androidApi`); build types `debug` and
  `release`; task `assembleModernDebug`.
- Entry point: `MainActivity` → `PluviaMain()` → `NavHost` with `WoWLauncher` and `XServer` routes;
  `WoWLauncher` is `startDestination`. Main/Settings/FirstInstall are internal screen states inside
  `WoWForeverScreen.kt`; Quick Menu is a Compose overlay.
- Java/XML UI is negligible (`frame_rating.xml` only); Java is mostly the Winlator engine.
- Existing fonts are Bricolage Grotesque only; Space Grotesk and JetBrains Mono will be fetched.
- Device is connected over Wi-Fi ADB (`10.0.1.92:5555`); never commit the address or serial.
- Method required by the user: brainstorming → design → plan approval → git worktree →
  subagent-driven development with per-task review → TDD → code review → verification → finishing branch.
- New work must not introduce dependencies beyond the font files.

## Approaches

1. **In-place visual swap for selected screen branches, debug-only** (chosen after builder check).
   `WoWForeverScreen.kt` keeps its ~40 state variables, navigation machine and every callback. Inside the
   `MAIN` and `SETTINGS` branches, a `useGameDesign = BuildConfig.DEBUG` flag selects either the legacy
   Material3 tree or new presentational composables wrapped in `GameTheme`. Cost: two visual trees coexist
   in debug; benefit: state and business logic are untouched and release stays byte-for-byte unchanged.
2. Fully parallel new screens whose own state machine duplicates the launcher's. Rejected: duplicates ~40
   variables and invites behavior drift, which conflicts with "no business logic changes".
3. Recolor existing Material3 widgets. Rejected: breaks the explicit "no material3 in new code" rule.

Quick Menu lives inside `XServerScreen`, not in `PluviaMain`. The plan touches only its single `QuickMenu(...)`
call site: in debug, substitute `GameQuickMenu(...)` wrapped in `GameTheme`. No other game-screen logic changes.

Settings depth: the three Settings sections (Launcher / Game / Graphics) get custom panels, buttons,
toggles, checkboxes and dropdowns with the same callbacks. Login, update and permission dialogs remain
legacy overlays; they are outside the three redesigned screens. This keeps the diff reviewable without
dropping functionality.

Quick Menu depth: the shell, tabs and action rows are restyled with custom primitives. All existing
`QuickMenuAction` handlers and state stay as they are.

Glow rule: `GlowButton` gains an `active: Boolean = false` parameter. Glow follows the single active
element owned by the screen, not merely `enabled`. Two enabled buttons do not both glow.

Fonts and motion: bundle static Space Grotesk Regular / Medium / Semibold / Bold and JetBrains Mono
Regular / Medium. One orchestrated 220 ms screen entrance animation is kept; when
`ANIMATOR_DURATION_SCALE == 0`, duration is 0 and no per-element fade/slide is used.

**Background layer:** the two existing GLSL files belong to the X Server render pipeline. Wiring them
into the launcher would require JNI/GLSL changes, which are out of scope. The chosen equivalent is a
single quiet Canvas grid in Compose using token `stroke` color, behind content, not over it. If a real
shader background is required, that is a separate native task and needs the user's approval.

## Decisions already made with the user

- Screens: launcher Main, Settings, Quick Menu (option 1).
- Fonts: allowed to download Space Grotesk and JetBrains Mono from Google Fonts.
- Build: existing `modernDebug`, no `designPreview` flavor.
- Screenshots: device is now attached over Wi-Fi; still add `@Preview` for tablet/foldable.

## Architecture and file layout

- `app/src/main/java/app/gamenative/ui/gameui/theme/GameTheme.kt` — adapted from the provided file;
  package replaced; fonts wired via `Font(R.font...)`; reduced-motion helper added.
- `app/src/main/java/app/gamenative/ui/gameui/components/NeonPanel.kt`, `GlowButton.kt`, `HudLabel.kt`.
- `app/src/main/java/app/gamenative/ui/gameui/screens/GameLauncherMain.kt`, `GameSettingsScreen.kt`,
  `GameQuickMenu.kt` — presentational only; every piece of state and every callback is passed in from
  the existing screen. No new state machine.
- `app/src/main/java/app/gamenative/ui/gameui/components/` also holds custom toggle, checkbox and
  dropdown primitives needed by Settings and Quick Menu, so no material3 widget is used there.
- `app/src/main/java/app/gamenative/ui/gameui/previews/GamePreviews.kt` — phone/tablet/foldable previews.
- `app/src/main/res/font/space_grotesk_*.ttf`, `jetbrains_mono_*.ttf`.
- `WoWForeverScreen.kt` and `XServerScreen.kt` get only the debug switch at the existing call sites;
  no new material3 imports. `PluviaMain.kt` is unchanged.

Component contracts:

- `NeonPanel(active: Boolean = false, content: @Composable () -> Unit)` — glow only when active.
- `GlowButton(text: String, onClick: () -> Unit, enabled: Boolean = true, active: Boolean = false,
  modifier: Modifier = Modifier)` — `Role.Button`, `contentDescription = text`,
  `defaultMinSize(48.dp, 48.dp)`.
- `HudLabel(label: String, value: String, modifier: Modifier = Modifier)` — mono font for `value` only;
  `label` uses body font; label and value are both exposed to TalkBack.

## Verification plan

- Unit tests: token values and contrast ratios, reduced-motion duration helper, GlowButton enabled/disabled
  state mapping, NeonPanel active glow mapping.
- `./gradlew assembleModernDebug` and `./gradlew testModernDebugUnitTest`.
- Install the debug APK over Wi-Fi ADB, open the first screen, capture portrait screenshots, check the app
  does not crash (`adb logcat` filtered to errors).
- TalkBack-oriented check via semantics/`uiautomator dump` for interactive elements; contrast computed in
  tests; reduced motion tested as a pure helper.
- Report changed files, commands and their output, and known limitations.

## Left to the implementation plan

Exact screen parameter lists, task split across the named subagents, per-task review gates, test names,
and the final screenshot/verification commands.
