---
name: Classic Forever
description: A monochrome, high-contrast serif launcher reproduced from the user's Figma screens.
colors:
  screen-black: "#000000"
  paper-white: "#FFFFFF"
  quiet-grey: "#8A8A8A"
  divider-grey: "#3A3A3A"
typography:
  display:
    fontFamily: "Cinzel Decorative, serif"
    fontSize: "48sp"
    fontWeight: 400
    lineHeight: 1.1
  title:
    fontFamily: "Cinzel Decorative, serif"
    fontSize: "36sp"
    fontWeight: 700
    lineHeight: 1.1
  body:
    fontFamily: "Cinzel, serif"
    fontSize: "18sp"
    fontWeight: 400
    lineHeight: 1.5
  button:
    fontFamily: "Cinzel, serif"
    fontSize: "18sp"
    fontWeight: 400
    lineHeight: 1.4
  caption:
    fontFamily: "Cinzel, serif"
    fontSize: "13sp"
    fontWeight: 400
    lineHeight: 1.4
rounded:
  control: "12dp"
spacing:
  base: "16dp"
components:
  button-outline:
    backgroundColor: "{colors.screen-black}"
    textColor: "{colors.paper-white}"
    rounded: "{rounded.control}"
    padding: "12dp 24dp"
    height: "48dp"
  field:
    backgroundColor: "{colors.screen-black}"
    textColor: "{colors.paper-white}"
    rounded: "{rounded.control}"
    padding: "12dp 16dp"
    height: "56dp"
  row:
    backgroundColor: "{colors.screen-black}"
    textColor: "{colors.paper-white}"
    rounded: "{rounded.control}"
    padding: "12dp 16dp"
---

# Design System: Classic Forever

## Overview

**Creative North Star: "The Quiet Title Screen"**

This design is an exact visual reproduction of the five Figma screens supplied by the user. It replaces the previous AMOLED-minimal system. The visual language is monochrome, typographic and deliberately spare: pure black ground, white high-contrast serif lettering, thin grey outlines and no decorative colour. The only material is type and the 1px rule.

The launcher is not trying to look like a modern app dashboard. It reads like a title screen from a classic PC game: large serif headlines, small underlined or outlined commands, and long empty space that makes the few controls feel deliberate. No iconography is used on these screens. The player's task — log in, configure, launch — is expressed entirely through words.

**Key Characteristics:**
- Pure black, white and two greys; no accent colour
- Cinzel for every glyph, Cinzel Decorative for headings
- Thin rounded 1px outline controls, never filled
- Text-only interface; no icons on the launcher surfaces
- Real device/build values only; no invented numbers

## Colors

Four roles, no accent.

### Primary
- **Paper White** (`#FFFFFF`): every heading, label, value and button text.

### Neutral
- **Screen Black** (`#000000`): every background, field and button fill.
- **Quiet Grey** (`#8A8A8A`): inactive tabs, muted values, version block.
- **Divider Grey** (`#3A3A3A`): 1px outlines and rules.

### Named Rules
**The No-Accent Rule.** Nothing in this system is coloured. State is expressed by weight, case, opacity and an outline, never by hue.

## Typography

**All Text:** Cinzel. **Headings and the two-line launcher title:** Cinzel Decorative. This is a user-pinned decision.

**Character:** high-contrast, literary and ceremonial. The thin strokes and generous tracking make the interface feel like a title card rather than a settings panel.

### Hierarchy
- **Display** (Cinzel Decorative, 48sp): centered welcome line and large section headings.
- **Title** (Cinzel Decorative, 36sp bold): active sidebar tab, major section names.
- **Body** (Cinzel, 18sp): labels such as ACCOUNT NAME, PASSWORD, RESOLUTION.
- **Button** (Cinzel, 18sp): all outlined commands, sentence case in the Figma source.
- **Caption** (Cinzel, 13sp): version block and muted values.

### Named Rules
**The All-Cinzel Rule.** No second family and no icon font appears on these surfaces; even numeric values are set in Cinzel.

## Layout

The wide layout is a three-zone composition. A left sidebar holds LAUNCHER / GAME / COMPONENTS, separated from the content by a single vertical hairline. The content area places labels on the left and their outlined action on the right, with a thin horizontal rule between groups. CLOSE sits at the bottom-right and the version block at the bottom-left.

The login screen places the two-line product title at the top-left, the credential form in the centre, CREATE A ACCOUNT / COMMUNITY SITE at the lower-left, and LAUNCH / SETTINGS at the lower-right. The welcome screen centers its heading at the top, lists numbered steps with right-aligned actions, and keeps LAUNCH and the version block at the bottom.

On phone portrait the same zones stack in reading order but preserve the exact type, outlines and bottom action cluster. On tablet and foldable the wide composition is reproduced 1:1.

## Elevation & Depth

Flat. There are no shadows, fills or tonal layers. Depth is drawn only with 1px outlines and empty space.

## Shapes

One form: a rounded rectangle with a 12dp corner radius and a 1px `Divider Grey` stroke. No filled buttons, no radius larger than 12dp, no square corners on controls.

## Components

### Outlined Button
- **Shape:** 12dp radius, 1px `#3A3A3A` stroke.
- **Fill:** none (screen black).
- **Text:** Cinzel 18sp, paper white.
- **States:** active/hover adds white text weight; disabled uses quiet grey. No colour change.

### Text Field
- 12dp radius, 1px quiet outline, black fill, white text, 56dp height.
- Labels sit above in small Cinzel caption.

### Sidebar Tab
- Inactive: quiet grey, no outline.
- Active: paper white, bold, wrapped in the same 12dp outline.

### Row
- A label on the left and an outlined action on the right, separated by generous space and a hairline below the group.

### Version Block
- Two lines, quiet grey, 13sp: `APP <actual version>` and `VERSION CLIENT <actual build>`.

## Do's and Don'ts

### Do:
- **Do** reproduce the five Figma screens literally in composition and type.
- **Do** use `BuildConfig.VERSION_NAME` and the real client build; never display the mock's 0.3.0.
- **Do** keep every control at least 48dp high on touch devices.
- **Do** keep system Back and TalkBack names on every control.

### Don't:
- **Don't** add colours, icons, gradients, shadows or filled buttons.
- **Don't** introduce a second typeface.
- **Don't** invent FPS, online counts or driver names; read detected values.
- **Don't** auto-launch the game from any screen except the explicit LAUNCH action.
