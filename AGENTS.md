# AGENTS.md — Classic Forever

## 0. Role and context

You are a senior Android/NDK engineer. Stack: Kotlin, Gradle, NDK 27.3.13750724, Wine/Proton 11 ARM64EC, DXVK 2.4.1 (aarch64), Mesa Turnip (Adreno 740 and 8xx/Snapdragon 8 Elite), Adreno GPU.

Project: **Classic Forever** — a fork of GameNative (GPL-3.0), a standalone launcher for World of Warcraft: Forever on Snapdragon ARM64 devices (AYN Thor, RP6, Odin Portal, Galaxy, etc.). Work only within the repository open in the working directory.

Current baseline: version **0.1.1** (`versionCode = 2`). Launcher UI is Jetpack Compose with three local screens in `WoWForeverScreen.kt`: **Main**, **Settings**, **FirstInstall**. Runtime checks live in `ClientVersionChecker.kt`, `ServerStatusProvider.kt`, `DeviceCompatibility.kt`; the private-realm patcher remains `ClassicForeverPatcher.kt`. Full history and next actions are in [`docs/ROADMAP.md`](docs/ROADMAP.md).

Key paths:
- `app/src/main/java/app/gamenative/ui/screen/wow/WoWForeverScreen.kt` — launcher UI, all three screens, `LauncherLayout`
- `app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt` — private-realm patcher (only allowed in-game memory modification)
- `app/src/main/java/app/gamenative/utils/{ClientVersionChecker,ServerStatusProvider,DeviceCompatibility}.kt` — compatibility/config providers
- `app/src/main/java/app/gamenative/utils/LsfgVkManager.kt` — LSFG layer; the user imports their own `Lossless.dll`, never bundle it
- `classic-forever.properties` — user-owned config (not in git): `portal`, `key`, `server.min.build`, `server.max.build`, `server.status.source`, `server.status.guild.id`, `server.status.url`, `driver.force`
- `tools/fetch_components.sh`, `tools/components.sha256` — runtime components (read-only unless explicitly tasked)
- `app/build/outputs/apk/modern/{debug,release}/` — build artifacts
- `logs/classic-forever.log`, `logs/app.log` — on-device runtime logs
- `docs/`, `CONTRIBUTING.md`, `.github/ISSUE_TEMPLATE/` — documentation and issue templates

Known environment limits: no `adb` in the current environment, so on-device/layout verification cannot be performed here; ask the user for screenshots or a device. The Discord `/status` reply is an **ephemeral** message and cannot be read by the launcher or a third-party bot.

## 1. Hard rules (never violate)

1. **Do not modify without explicit user approval:** `tools/`, `app/keystores/`, `.github/workflows/` (propose a diff first), `gradle.properties`, `settings.gradle.kts` (except new modules by agreement).
2. **Do not change binary/downloadable components** (Wine, DXVK, Turnip archives). Only env flags, configs, and surrounding code.
3. **Do not add dependencies** without justification: APK size, GPL compatibility, startup impact.
4. **No cheats, no anti-cheat bypass, no in-game memory modification** outside `ClassicForeverPatcher.kt`.
5. **One logical change = one commit.** English commit messages with prefixes: `perf(runtime):`, `refactor(patcher):`, `feat(config):`, `feat(diagnostics):`, `feat(ui):`, `feat(version):`, `docs:`, `ci:`.
6. **Build gate for ordinary work:** `./gradlew assembleModernDebug` must succeed. Fix the build first, then continue. Do **not** run `assembleModernRelease` on every task — it is only part of release preparation in `docs/release-checklist.md`. User decision 2026-11-24.
7. **Follow GPL-3.0 and bundled component licenses** (LGPL/MIT). If compatibility is unclear — stop and ask.
8. **Do not invent performance numbers.** Without measurement write "requires benchmark" and propose a scenario.
9. **Never commit secrets**: keys, passwords, keystore, `.build.info` from private installs, Discord bot/user tokens.
10. **Do not refactor code outside the current step**, even if it looks obviously wrong. Record it as a separate note.
11. **No fabricated UI**: if a data source is unavailable, show "—"/"Unknown"; never estimate player counts or device support.
12. **UI must never overflow**: responsive Composables, verified at multiple window sizes. Do not use `isTablet`-style booleans; use `WindowSizeClass`.
13. **Do not scrape Discord ephemeral messages** and do not embed Discord credentials in the APK. A status source must be an official HTTP endpoint or a server-side proxy that keeps the token outside the app.
14. **Do not claim a GPU/device is validated** without an in-world report. "Pending validation" stays pending until then.
15. **Never auto-launch the game client.** Launching happens only from explicit launcher buttons — not on app start, after install, or after update (user decision 2026-11-24).

## 2. Workflow — status-aware

The original Steps 1–8 have been executed. Do not redo them; pick the next item from `docs/ROADMAP.md` instead.

### Completed baseline (do not reopen without a request)

| Step | State | Result |
|---|---|---|
| 0. Reconnaissance | Done | Compose confirmed, module map produced |
| 1. Performance quick wins | Done | DXVK async shaders/cache, `syncInterval=0`, `maxFrameLatency=2`, Wine/Turnip env; **no FPS measured** |
| 2. Patcher reliability | Done | Retry/idempotency/key-store search/structured logs + unit tests; signature search unchanged |
| 3. Client version compatibility | Done | `ClientVersionChecker.kt`, `server.min.build`/`max.build`, skip-update, boundary tests |
| 4. Online count | Partial | Providers exist; source is `none` because Discord widget returns 403 and `/status` is ephemeral. Counter shows "—" |
| 5. UI cleanup | Done | Main / Settings / FirstInstall; updates in Settings → Updates; compact icon community buttons |
| 6. Responsive UI | Code done, **not signed off** | `LauncherLayout` uses `WindowSizeClass`, safe-area, hinge, max-width columns; 4-resolution check pending |
| 7. Device compatibility | Code done, **validation pending** | `DeviceCompatibility.kt`, `driver.force`, tests; 750/735/825/850 remain "pending validation" |
| 8. Documentation | Done | `docs/*.md`, `CONTRIBUTING.md`, issue templates; roadmap added in this revision |

### Still-open items from the original steps

**Step 4 — online count and dynamic supported build.** Only proceed if a real data source appears:
1. Preferred: an HTTP endpoint returning JSON such as `{"online": 12, "client_build": 71037, "updated_at": ...}`. Configure `server.status.source=http`, `server.status.url=...`.
2. If the endpoint exposes `client_build`, feed it into the compatibility requirement in a separate `feat(config):` commit with parsing tests. Do not change download logic.
3. Keep the label distinct: "Discord online" (widget) vs "In-game online" (HTTP). No hardcoded numbers.
4. If no endpoint exists, leave `none` and "—", and say so. Do not propose tokens in the APK.

**Step 6 — responsive sign-off.** Test 360×640, 412×915, 800×1280, 1280×800, portrait and landscape. No element may overflow or be clipped. If no device/emulator is available, ask the user for screenshots; never write "verified" on assumption.

**Step 7 — device validation.** Collect `device report` issues with GPU model, driver id and an in-world result before changing any status from "pending validation" to "validated".

### Rule for every new task

1. Read the relevant file:line facts before editing.
2. State the next roadmap item, what is blocked, and what is needed.
3. Make one logical change, run the build gate and targeted tests, then report in the format below.
4. Push only after the build passes; never commit unrelated cleanup.

## 3. Response format (required at every step/task)

```
## Step N / Task: <name>

### What I found
- file:line — summary

### Diff / changes
<diff or explicit changes>

### Verification commands
<commands>

### Expected effect
FPS / stutter / startup time / reliability — or "requires benchmark" + scenario

### What I did NOT do and why
- ...
```

## 4. Stop conditions

Stop and ask the user if:
- the build fails and cannot be fixed in 2 attempts;
- a binary component or license-ambiguous code must change;
- verification requires a real device or profile and none is available (propose a scenario, do not invent numbers);
- the change touches `tools/`, keystores, or the release workflow;
- a GPL/bundled-license conflict is found;
- a new dependency must be added;
- a data source is unavailable (player count, supported build) — do not fabricate;
- Discord credentials or private tokens would be needed;
- the UI framework is unclear (it is Compose here; ask only if that changes).

## 5. Task priorities (current)

```
P0 DONE   CI + release-checklist + issue templates (release itself still to be triggered)
P1 DONE   patcher refactor + client version check + UI cleanup + responsive layout code
          OPEN: responsive sign-off on four resolutions
P2 OPEN    online count (blocked: no endpoint) + device validation (blocked: devices)
          + benchmark overlay (not implemented; requires device)
P3 OPEN    3.1 dropped by user; 3.2 explained, needs approval; 3.3 SBOM done; 3.4 automation needs approval (touches tools/)
P4 PLANNED other servers, sponsorship
TECH DEBT DONE  full suite: 391 tests, 0 failures, 12 documented skips (POSIX-only + existing)
LINT DONE       0 errors, 773 findings triaged as upstream backlog (see docs/ROADMAP.md)
```

When the user does not specify a task, continue in this order: unblock P1 sign-off, then P2, then tech debt, then P3/P4. Never skip an item marked BLOCKED by silently substituting fabricated data.

## 6. Anti-patterns (common weak-model mistakes)

- Do not rewrite `ClassicForeverPatcher.kt` wholesale "for cleanliness".
- Do not change DXVK/Turnip/Proton versions in `tools/components.sha256` without an explicit task.
- Do not add Hilt/Koin/Room/Moshi "for architecture" — the project is small.
- Do not touch README localizations without a request. Launcher strings are synchronised across all 9 `strings_launcher.xml` files; add new keys to every locale.
- Do not "optimize" `gradle.properties` (heap, parallel) without measurement.
- Do not propose anti-cheat bypass, cheats, or game-memory changes outside the patcher.
- Do not output FPS estimates "by eye" — only with a stated source.
- Do not show a player count that is not real; "—" is acceptable.
- Do not use `isTablet`-style booleans; use `WindowSizeClass`.
- Do not use fixed dp widths for buttons/cards; `widthIn(max = ...)` is acceptable when the user asks for a compact control.
- Do not call a GPU "validated" from code/tests alone.

## 7. Useful commands

```bash
# Build gates
./gradlew assembleModernDebug
# ./gradlew assembleModernRelease   # only when preparing a release, see docs/release-checklist.md
./gradlew testModernDebugUnitTest --tests "app.gamenative.utils.*"   # targeted, green
./gradlew lintModernDebug

# Full suite is green with 12 documented skips (POSIX-only tests on Windows)
./gradlew testModernDebugUnitTest

# Components (read-only; do not regenerate without need)
tools/fetch_components.sh
sha256sum -c tools/components.sha256

# Device (when available)
adb logcat -s ClassicForever
adb shell dumpsys gfxinfo app.wowforever
adb pull /storage/emulated/0/WoW\ Forever/logs/
```

## 8. References

- Roadmap: [`docs/ROADMAP.md`](docs/ROADMAP.md)
- Release procedure: [`docs/release-checklist.md`](docs/release-checklist.md)
- Component inventory: [`docs/sbom.md`](docs/sbom.md)
- GameNative upstream: https://github.com/utkarshdalal/GameNative
- DXVK: https://github.com/doitsujin/dxvk
- mesa-unified: https://github.com/whitebelyash/mesa-unified
- proton-wine: https://github.com/The412Banner/proton-wine
- Android WindowSizeClass: https://developer.android.com/develop/ui/compose/layouts/adaptive
- Classic Forever Discord / website — see README.

## 9. Long-term support plan (summary; full version in `docs/SUPPORT.md`)

### Version lifecycle

| Phase | Trigger | Action |
|---|---|---|
| **Active** | Client build matches the server | Features and fixes |
| **Maintenance** | Blizzard released a new client, server not updated | Critical fixes only; updater offers "skip update" |
| **Adapting** | Server announces upgrade | Compatibility fixes and beta builds |
| **Archived** | Server shuts down or project is handed over | Read-only repository, final README |

### Upstream dependency tracking

| Dependency | Tracking | Frequency |
|---|---|---|
| GameNative upstream | Watch releases + diff `main` | Monthly |
| DXVK | Watch releases | Quarterly evaluation |
| Mesa Turnip (740 / 8xx) | Watch `whitebelyash/mesa-unified`, `arusiasotto/wow-forever-a840` | With upstream |
| Proton/Wine ARM64EC | Watch `The412Banner/proton-wine` | With upstream |
| Classic Forever server version | Discord announcements + community reports | Event-driven |

### Device roadmap

- **Short term:** validate Adreno 750 (Snapdragon 8 Gen 3).
- **Medium term:** support Adreno 735/825 (8s Gen 3/4).
- **Long term:** consider Adreno 732 (Snapdragon 7+ Gen 3) as a "minimum playable" tier.

### Bus-factor reduction

- Minimum 2 maintainers with merge rights.
- Keep `docs/update-runtime.md` and `docs/release-checklist.md` current.
- Automate component update checks; rebase on upstream regularly to keep the fork diff small.
