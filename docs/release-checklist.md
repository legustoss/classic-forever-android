# Release checklist

1. Confirm the target version in `app/build.gradle.kts` (`versionName`, `versionCode`).
2. `./gradlew assembleModernDebug` — must succeed.
3. `./gradlew testModernDebugUnitTest` — green.
4. `./gradlew assembleModernRelease` — must succeed.
5. Verify the release signing key in GitHub Actions secrets:
   - `RELEASE_KEYSTORE_BASE64`
   - `RELEASE_STORE_PASSWORD`
   - `RELEASE_KEY_ALIAS`
   - `RELEASE_KEY_PASSWORD`
6. Push the release commit to `main`.
7. **Actions → Release → Run workflow**:
   - `ref`: `main`
   - `tag`: exactly `v<versionName>` (e.g. `v0.1.1`)
   - `title`: `Classic Forever v0.1.1`
   - `notes`: release notes
   - `prerelease`: **off** for a normal release (the in-app updater ignores prereleases)
8. The workflow verifies the signature with `apksigner`, rejects debug keys, uploads `Classic-Forever.apk`, and removes temporary signing files.

## Do not

- Do not change the release key between consecutive releases; Android will refuse the update.
- Do not commit keystores, passwords or `.build.info` from a private install.
- Do not modify `.github/workflows/` without an explicit request.

See [SUPPORT.md](SUPPORT.md) for version lifecycle.
