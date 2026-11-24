# Release checklist

## Versioning policy

Semantic-version-like, adapted to a 0.x project:

- **Patch** (`0.1.1` → `0.1.2`): bug fixes, config/lint/docs changes, no new user-facing feature.
- **Minor** (`0.1.1` → `0.2.0`): a new user-facing feature or a significant UI rework. LSFG frame generation, the three-screen launcher and the categorised Quick Menu fall here.
- **Major** (`0.x` → `1.0.0`): only when the project is considered stable for a public release. Component version bumps alone never justify a major.
- `versionCode` increases by 1 for every release, even when `versionName` stays the same. The tag is exactly `v<versionName>` and the workflow checks it.
- Release in preparation: **v0.2.0** (`versionCode = 3`). Final publish waits for the user's device comparison; test APKs are already built.

## Checklist

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
   - `tag`: exactly `v<versionName>` (for this release `v0.2.0`)
   - `title`: `Classic Forever v0.2.0`
   - `notes`: release notes
   - `prerelease`: **off** for a normal release (the in-app updater ignores prereleases)
8. The workflow verifies the signature with `apksigner`, rejects debug keys, uploads `Classic-Forever.apk`, and removes temporary signing files.

## Do not

- Do not change the release key between consecutive releases; Android will refuse the update.
- Do not commit keystores, passwords or `.build.info` from a private install.
- Do not modify `.github/workflows/` without an explicit request.

See [SUPPORT.md](SUPPORT.md) for version lifecycle.
