# Runtime components

The launcher does not include Blizzard game files. It bundles Wine/Proton, DXVK and Turnip archives fetched at build time.

| Component | Version / file | Source |
| :--- | :--- | :--- |
| Proton/Wine ARM64EC | `proton-11.0-90624-arm64ec.wcp` | `components-v1` release of `jaredgei/wow-forever-android`, built from [The412Banner/proton-wine](https://github.com/The412Banner/proton-wine) |
| DXVK | `dxvk-2.4.1-wow-aarch64-test.wcp` | `components-v1` release, from [doitsujin/dxvk](https://github.com/doitsujin/dxvk) |
| Turnip (Adreno 740) | `turnip-wow-scheduler-test.zip` | `components-v1` release, Mesa with scheduler fix |
| Turnip (Adreno 8xx) | `turnip-V32-RP6sched.zip` | [arusiasotto/wow-forever-a840](https://github.com/arusiasotto/wow-forever-a840), `turnip/gen8` v32 |

Checksums are authoritative in [`tools/components.sha256`](../tools/components.sha256):

```
137807f4145b1b440075f83b2b1b3501d19c4eb618784b1a28418652cae3db03  proton-11.0-90624-arm64ec.wcp
f660b5c6422b3f93cec65664f309718942e58581b5b96fa2902d55d63ce1db8a  dxvk-2.4.1-wow-aarch64-test.wcp
02384f692735515f73aad8a544bc640abe3f35fe18e8df2e667be712c442e54c  turnip-wow-scheduler-test.zip
05434d3c102563a4577f514410cc713494bcf900f52c2b935391adf87a76b77a  turnip-V32-RP6sched.zip
```

`COMPONENTS_URL` overrides the release base URL for the first three files. The Adreno 8xx driver is fetched from the raw GitHub URL in the script.

## Provenance and update watchlist

`components-v1` was published 2026-09-30 by the `jaredgei/wow-forever-android` community bundle. Its README records the exact upstream revisions:

- Proton/Wine ARM64EC: [The412Banner/proton-wine](https://github.com/The412Banner/proton-wine/tree/e5fa703ed7f7329e20d7ede481ab185cf8b1a8b2) with an ARM64 copied-syscall fix and an NLS fallback allocation patch.
- DXVK 2.4.1 aarch64: [doitsujin/dxvk](https://github.com/doitsujin/dxvk/tree/0cf05780abd7250c2cd713b7749cf32180157cf5).
- Turnip (Adreno 740): Mesa [`fe067b17d9`](https://github.com/mirror/mesa/tree/fe067b17d9) with a patch limiting barycentric waits to the current block (fixes a shader-scheduler assertion on Adreno 740).

Observed 2026-11-24 (read-only research):

- No `components-v2` tag or newer component release exists in that repository; the latest app release is `v2.4.6`.
- Upstream Proton-Wine has moved since `e5fa703`: v15 (2026-10-23) adds GameNative fixes and WinRT registry registration; a 2026-10-24 fix treats `C.<charset>` like `C` for locale mapping. Both are relevant to Android but are **not** in our archive.
- The parent project's `v2.4.4` removed 32-bit Windows files from its bundled Wine (APK shrank ~50 MB). Our `components-v1` archive predates that change, so a repack could reduce size — but only after device validation.
- Newer generic Turnip/DXVK builds exist in the wider GameNative ecosystem. They are not necessarily patched for WoW Forever's aarch64/D3D11 path.

Adoption rule: watch the component release tag and upstream revisions, compare hashes and patch lists, then propose a diff in `tools/components.sha256` for explicit approval. Never swap a binary component without an in-world device test; device compatibility takes priority over being current.

Concrete candidates and their compatibility caveats are tracked in [performance-candidates.md](performance-candidates.md).

## Runtime env vars

Set in `WoWForeverScreen.containerConfig()` and `XServerScreen`:

- Wine: `WINEDEBUG=-all`, `WINEESYNC=0`.
- DXVK: async shaders and async pipeline cache via `dxwrapperConfig`; `dxgi.syncInterval=0` and `dxgi.maxFrameLatency=2` in `DXVK_CONFIG`.
- Mesa: `MESA_SHADER_CACHE_DISABLE=false`, `MESA_SHADER_CACHE_MAX_SIZE=1024MB`, `MESA_VK_WSI_PRESENT_MODE=mailbox`.
- Turnip: `TU_DEBUG` from `DeviceCompatibility` (740 → `noconform`, 8xx → `noconform,sysmem`).

See [update-runtime.md](update-runtime.md) before changing versions.
