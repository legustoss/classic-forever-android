# Updating a runtime component

Runtime archives are downloaded at build time and verified with `tools/components.sha256`. Never replace them blindly.

## Safe procedure

1. Obtain the new archive from the upstream project named in [runtime.md](runtime.md).
2. Verify its origin and license. Do not use unofficial binaries.
3. Put the file in a temporary folder and compute:
   ```bash
   shasum -a 256 /path/to/new-archive
   ```
4. Update the matching line in `tools/components.sha256`.
5. Remove the old file from `app/src/main/assets/bundled_components/` and run:
   ```bash
   tools/fetch_components.sh /path/to/components
   ```
6. If the component id changes (for example a new DXVK version name), update the constants in `WoWForeverScreen.kt` (`DXVK_VERSION`, `PROTON_VERSION`, driver names) and any related env vars.
7. Run:
   ```bash
   ./gradlew assembleModernDebug
   ./gradlew testModernDebugUnitTest
   ```
8. Test on a real device before releasing. Record the result in [devices.md](devices.md) and [benchmarks.md](benchmarks.md).

## Never

- Do not edit `tools/components.sha256` to match an unknown file.
- Do not commit the downloaded archives — `app/src/main/assets/bundled_components/` is gitignored.
- Do not change a driver just because a newer version exists; validate the game first.
