# AMOLED minimal design — design (iteration 2)

## Intent and why

The first token-based redesign was rejected by the user: too many buttons, cramped labels wrapped
letter-by-letter, actions scattered across the screen, and controls placed too high to reach comfortably.
Final review independently confirmed these problems: four-line wrapped Settings labels, lost Website/Discord
links, missing launch/update status, and TalkBack labels attached to non-focusable sibling nodes.

This iteration replaces, not patches, the rejected visual layer. Business logic, navigation, native code,
API and DB stay untouched.

## Goals

- AMOLED base: background `#000000`, primary text `#FFFFFF`, secondary text `#A7B0BA`, borders `#2A2A2A`.
  The unit test asserts secondary-on-black contrast >= 4.5:1 (actual about 8.5:1).
- One accent only, chosen by the designer: muted cyan `#7FE9DC`. No pink/signal colour at all. Warnings
  use white text plus an icon, never a second accent colour.
- Minimal main screen: title, one line of status, and exactly three buttons in the bottom third:
  **Играть**, **Настройки**, **Ещё**. The **Ещё** sheet contains Сайт, Discord and Вход в аккаунт.
- All other actions live in Settings tabs: files, addons, LSFG, components, updates, account management,
  client/app versions.
- Thumb-first: in portrait, every interactive control sits in the lower 40% of the screen; nothing
  important requires reaching the top. In landscape, the same controls align to the bottom-right.
- No text wrapping by letter: single-line labels with ellipsis, sensible minimum widths, two rows instead
  of four equal-weight buttons. All touch targets >= 48dp.
- Restore everything the first iteration lost: Website/Discord links, launch/update progress and status,
  and TalkBack names on the actual focusable nodes.
- Remove the decorative background grid — pure black is the design.
- Keep reduced motion: one 220 ms entrance on the Main screen only, zero when
  `ANIMATOR_DURATION_SCALE == 0`. No animation abstraction beyond the existing `motionDuration` helper.
- Legacy Material3 dialogs (container config, controller/gyro/gesture editors, login, update, permissions)
  stay as-is. They are opened from Settings and are not part of the three redesigned screens. Their
  existing dark PluviaTheme colors are close to pure black; restyling them would multiply scope and is a
  known visual inconsistency, not a functional regression.
- Settings tab selector moves to the **bottom** of the screen, next to the compact back control, so every
  interactive element stays in thumb reach. Content scrolls above it. Landscape uses the same bottom row.
- Launch/update feedback is one status line plus a thin monochrome progress bar shown only while active;
  the progress node is labelled for TalkBack. No second colour.
- In landscape, title, status and the three buttons all align to the lower-right block; the only control
  outside that block is a compact back button at the lower-left. Nothing interactive is at the top.

## Non-goals / anti-goals

- No changes to C/C++/JNI/NDK, GLSL, shaders, business logic, API or DB schemas.
- No new dependencies, flavors, gradle changes, version changes or release build.
- No neon-on-black cliche: no glow on passive panels, no all-caps eyebrows, no `→` in buttons, no
  identical cards with soft grey shadows, no per-element fade-and-slide.
- No MaterialTheme/Material composables in new code. `androidx.compose.material3.windowsizeclass.*`
  measurement APIs remain exempt platform types.
- No top-bar back button in portrait; landscape keeps a compact back control.

## Constraints

- Single `app` module, flavor `modern`, gate `assembleModernDebug`; version stays `0.2.0`.
- Device is reachable over Wi-Fi ADB; device address/serial must never enter git.
- Baseline for the new branch is `main` at the current docs commit, not the rejected design branch.
- Required method: design approval → plan approval → new git worktree → subagent-driven development with
  per-task review → TDD → whole-branch review → verification → finishing branch.
- Named subagents only, model `opencode/hy4-flash-free` as configured by the user.

## Approaches

1. **New branch from main, replace the visual layer** (chosen). The rejected branch stays for history but
   is never merged. New `MonoTheme` primitives and three redesigned screens are built from main, so no
   rejected component leaks forward.
2. Patch the rejected branch in place. Rejected: inherits cramped geometry and the dropped callbacks;
   diff would be hard to review and easy to miss one of the reported regressions.
3. In-place restyle of the legacy Material3 widgets. Rejected: violates the no-material3 rule and keeps
   the same crowded layout.

## Layout decisions

Portrait main, bottom-anchored:

```
Classic Forever
one status line

        …

[      ИГРАТЬ      ]
[ Настройки ] [ Ещё ]
```

All three buttons are >= 56dp high with one-line labels. `Ещё` opens a modal sheet with three 56dp rows:
Сайт, Discord, Вход в аккаунт (the last row hidden when there is no saved login). Landscape mirrors the
same stack aligned bottom-right.

Settings keeps three tabs (Лаунчер / Игра / Графика). Actions are laid out in two rows with min widths;
no four-button equal row. Component groups stay expandable with one import button each. Quick Menu keeps
all actions but uses single-line labels and 48dp+ targets.

## Accessibility decisions

- `contentDescription` must be applied on the same semantics node as `clickable`/`Role`, before padding
  modifiers; verify with `uiautomator dump` that focusable nodes carry `content-desc`.
- `Ещё` sheet is a proper modal: labelled title, focus management, dismiss on back/outside.
- Status changes (launching, updating) are announced via a labelled progress/text node.
- Contrast: white on black far exceeds 4.5:1; grey secondary text is checked by a unit test.
- Reduced motion honoured at composition time; no live-toggle requirement.

## What is left to the plan

Exact file/package names, task split across subagents, test names, device verification commands, and the
final APK/report.
