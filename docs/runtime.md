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

## Runtime env vars

Set in `WoWForeverScreen.containerConfig()` and `XServerScreen`:

- Wine: `WINEDEBUG=-all`, `WINEESYNC=0`.
- DXVK: async shaders and async pipeline cache via `dxwrapperConfig`; `dxgi.syncInterval=0` and `dxgi.maxFrameLatency=2` in `DXVK_CONFIG`.
- Mesa: `MESA_SHADER_CACHE_DISABLE=false`, `MESA_SHADER_CACHE_MAX_SIZE=1024MB`, `MESA_VK_WSI_PRESENT_MODE=mailbox`.
- Turnip: `TU_DEBUG` from `DeviceCompatibility` (740 → `noconform`, 8xx → `noconform,sysmem`).

See [update-runtime.md](update-runtime.md) before changing versions.
