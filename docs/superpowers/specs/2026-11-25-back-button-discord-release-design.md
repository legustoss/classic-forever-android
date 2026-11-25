# Back button, Discord icon and v0.2.0 release — design

## Intent
The user confirmed the new Settings layout but wants one back target, not two, and a real Discord
icon in the community row. After that, changes are integrated and v0.2.0 is released.

## Goals
- Portrait (`WindowWidthSizeClass.Compact`): only the bottom Back button remains, and it is narrow
  (`widthIn(min = 140.dp)`) instead of `fillMaxWidth`.
- Landscape (non-Compact): only the top Back button in `ScreenTopBar` remains, with its existing
  clickable row and gamepad support.
- Community Discord button uses a real Discord logo vector drawable instead of `Icons.Default.Forum`.
- Integrate the branch to `main`, run `assembleModernRelease`, tag and push `v0.2.0`.

## Non-goals
- No changes to tabs, component groups, LSFG, selection logic or localization.
- No new dependencies; no changes to game input or auto-launch behavior.

## Constraints
- Version stays `0.2.0` (`versionCode = 3`); debug build + unit tests remain the routine gate.
- No secrets/personal data; `AGENTS.md` stays untracked; no `tools/`/keystore/workflow changes.
- GitHub Release page cannot be created here: no `gh` CLI and no token. Tag + APK are the deliverable.

## Approaches
- Back: one composable param `showBack` on `ScreenTopBar` plus conditional `BottomBackButton` at the
  call sites using the existing `wide` boolean (chosen — minimal, no new orientation logic).
  Alternative: separate orientation state — rejected, the project already uses window size class.
- Discord: a local vector drawable from simple-icons used with `painterResource` (chosen — no new
  dependency and keeps the existing neon tint). Alternative: a brand-icon library — rejected.

## Left to the plan
Tests for the back-button sizing helper if one is introduced, build/test/release commands.
