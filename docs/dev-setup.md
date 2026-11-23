# Development setup

From a clean clone to a built APK.

## Requirements

- JDK 17 or newer (the project compiles with Java 17; Gradle 8.12.1 works with JDK 21).
- Android SDK with platform 36, build-tools and NDK `27.3.13750724`.
- Git, curl/sh (for `tools/fetch_components.sh`).

`local.properties` must contain `sdk.dir=/absolute/path/to/Android/Sdk`. It is gitignored.

## Build

```bash
tools/fetch_components.sh                 # runtime archives, verified by tools/components.sha256
./gradlew assembleModernDebug             # app/build/outputs/apk/modern/debug/app-modern-debug.apk
./gradlew assembleModernRelease           # app/build/outputs/apk/modern/release/app-modern-release.apk
```

`assembleModernDebug` is the required gate before any pull request.

Useful checks:

```bash
./gradlew testModernDebugUnitTest
./gradlew lintModernDebug
```

Unit tests need network access on first run (JUnit/Robolectric artifacts).

## Runtime components

`tools/fetch_components.sh` downloads four archives into `app/src/main/assets/bundled_components/`. They are not stored in git. If the upstream release is unavailable, pass a folder with the four files:

```bash
tools/fetch_components.sh /path/to/components
```

See [runtime.md](runtime.md) for versions and checksums.

## App identity

- Package: `app.wowforever`
- Namespace: `app.gamenative`
- Version: see `app/build.gradle.kts` (`versionCode`, `versionName`)

Different signing keys cannot update each other. Use the same release key for consecutive releases.
