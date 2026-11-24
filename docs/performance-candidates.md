# Performance candidates (research, not adopted)

Snapshot: 2026-11-24. Status: **research only**. Nothing here changes `tools/`, `tools/components.sha256` or a bundled archive. Adoption requires explicit approval **and** an in-world device test.

## Baseline in this repo

| Component | Our build | Source revision |
|---|---|---|
| Proton/Wine ARM64EC | `proton-11.0-90624-arm64ec.wcp`, published 2026-09-30 | `The412Banner/proton-wine@e5fa703` + ARM64 copied-syscall fix + NLS fallback allocation patch |
| DXVK | `dxvk-2.4.1-wow-aarch64-test.wcp`, published 2026-09-30 | `doitsujin/dxvk@0cf0578`, custom aarch64 test build |
| Turnip 740 | `turnip-wow-scheduler-test.zip`, published 2026-09-30 | Mesa `fe067b17d9` + barycentric-wait patch |
| Turnip 8xx | `turnip-V32-RP6sched.zip` | `arusiasotto/wow-forever-a840`, V32 tree + RP6 scheduler skip |

## Candidates found upstream

### 1. Proton/Wine — most promising, but compatibility risk

`The412Banner/proton-wine` release `build-bionic-layers-20261030-v16` (current catalog default) includes `DroidDeck-proton-11.0-12.8-arm64ec.wcp`:

- URL: https://github.com/The412Banner/proton-wine/releases/download/build-bionic-layers-20261030-v16/DroidDeck-proton-11.0-12.8-arm64ec.wcp
- Size: 119,875,054 bytes; SHA-256 `a5e05e35214f34603f8cf9a362bb6b4bce9f18d773ee689ecfaff6136a1e9003`.
- Performance-relevant changes versus our older build: fewer extended-attribute queries (DroidDeck-Proton 11-12.6), faster missing-file lookups on case-folding volumes (11-12.7), fixed `RtlWaitOnAddress()` loading stall (main after 11-12.8), plus many Wine/GE-Proton stability fixes.
- Caveat: built for **DroidDeck-Bionic's runtime pack** (`tools/bionic-runtime` in Droid-Deck/DroidDeck), which pins it by SHA-256. Our container comes from GameNative's Bionic path, not DroidDeck's. It is **not** a guaranteed drop-in replacement. Upstream itself marks 11.0-12.8 as "not device-tested yet".
- Separate test layer `build-bionic-layers-20261118-v16-unwind` (`DroidDeck-Proton-11.0-12.8-arm64ec.wcp`, SHA-256 `3cc74b877bc8707d75aa2cc22e335373b6a6c0b6b1937c7b3c2cd98e55de2e89`) fixes ARM64EC unwind loops/hangs; reliability, not FPS.

### 2. DXVK — newer upstream, but no ready aarch64 EC artifact

- Upstream latest is `dxvk v3.1.1` (2026-09-15). Release notes include a Skyrim SE low-CPU-performance workaround and DXGI `DXGI_SCALING_NONE` support.
- The official tarball ships x86/x64 Windows DLLs only. Our `.wcp` is a custom **aarch64** test build from `0cf0578`, so 3.1.1 cannot simply replace it. Adoption would require rebuilding 3.1.1 for aarch64 with the same patches — a separate task for someone with that toolchain.

### 3. Turnip — no newer driver source

- `arusiasotto/wow-forever-a840`: `driver/turnip-V32-RP6sched.zip` last changed 2026-10-01 ("Adreno 840 Turnip build ... V32 tree plus RP6 scheduler skip"), i.e. effectively the same line we already ship.
- `whitebelyash/mesa-unified` last commit is 2026-05-14, older than our 740 archive. No newer 740 candidate.
- Generic newer Turnip/DXVK builds in the wider GameNative manifest are not patched for WoW Forever's aarch64/D3D11 path.

### 4. Size/startup idea from the parent app

Parent `v2.4.4` removed 32-bit Windows files from its bundled Wine (APK −50 MB, fewer AV false positives). Our `components-v1` archive predates that. A repack could reduce APK size and I/O, but it is a binary change needing approval and testing.

## Test plan (only after approval)

1. Keep the current build as baseline; record device, GPU, driver id and app version.
2. Run the scenarios in [`benchmarks.md`](benchmarks.md): login→realm, capital city stationary, camera rotation, first visit to a new zone, combat. Use Perfetto and `dumpsys gfxinfo app.wowforever`; record measured values only.
3. Swap **one** candidate at a time in a throwaway build; never overwrite a known-good archive without its hash saved.
4. If the candidate fails to boot or shows artifacts, revert and record the failure. Device compatibility wins over being current.
5. No FPS numbers are claimed here — all scenarios require a real device.

## Test build prepared 2026-11-24 (not committed as a source change)

- `app/build/outputs/apk/modern/debug/Classic-Forever-Proton-v16-TEST.apk` — debug APK with the DroidDeck-Proton 11.0-12.8 arm64ec `.wcp` embedded under the **existing** asset name `proton-11.0-90624-arm64ec.wcp`, so no code/container-config change was needed.
- `app/build/outputs/apk/modern/debug/Classic-Forever-baseline.apk` — the previous, already tested debug APK.
- Verified inside the test APK: embedded asset SHA-256 `a5e05e35214f34603f8cf9a362bb6b4bce9f18d773ee689ecfaff6136a1e9003`, size 119,875,054 bytes.
- After the build, the working asset was restored to `137807f4145b1b440075f83b2b1b3501d19c4eb618784b1a28418652cae3db03`, matching `tools/components.sha256`. `tools/` and `tools/components.sha256` were not modified.

**Important:** `installBundledComponents()` only extracts Proton when the app-private prefix is missing `profile.json` or `bin/wine`. Installing the test APK over an existing install will keep the old extracted Proton. Clear app data (or uninstall first) when switching to the test build; game files in `/storage/emulated/0/WoW Forever/` stay on shared storage, but the saved login in app-private storage will be lost.

Rollback: install `Classic-Forever-baseline.apk` and clear app data again so the original archive is extracted. Both APKs live only in `app/build/outputs/` (gitignored).

## Decision

Do not adopt anything into the repo yet. The test build is for measurement only; do not mark any candidate as "better" without device results.
