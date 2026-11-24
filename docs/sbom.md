# Software Bill of Materials (inventory)

This is a **manually maintained inventory**, not a generated CycloneDX/SPDX artifact. It lists everything that is part of a Classic Forever build, with the source-of-truth files for each group. Update it in the same commit as any dependency or runtime-component change.

## Application identity

| Field | Value |
|---|---|
| Package | `app.wowforever` |
| Namespace | `app.gamenative` |
| Version | 0.1.1 (`versionCode = 2`) |
| License | GPL-3.0 (fork of [GameNative](https://github.com/utkarshdalal/GameNative)) |
| Source | `app/build.gradle.kts` |

## Build toolchain

| Component | Version | Source |
|---|---|---|
| Gradle | 8.12.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.8.0 | `gradle/libs.versions.toml` |
| Kotlin | 2.1.21 | `gradle/libs.versions.toml` |
| KSP | 2.1.21-2.0.2 | `gradle/libs.versions.toml` |
| NDK | 27.3.13750724 | `app/build.gradle.kts` |
| compileSdk | 36 | `app/build.gradle.kts` |
| minSdk | 26 (modern flavour: 29) | `app/build.gradle.kts` |
| JDK | 17+ | `docs/dev-setup.md` |

## Bundled runtime components

Downloaded at build time into `app/src/main/assets/bundled_components/`, verified by `tools/components.sha256`; **not committed** to git.

| Component | File | SHA-256 | Upstream |
|---|---|---|---|
| Proton/Wine ARM64EC 11.0 | `proton-11.0-90624-arm64ec.wcp` | `137807f4145b1b440075f83b2b1b3501d19c4eb618784b1a28418652cae3db03` | `components-v1` release of `jaredgei/wow-forever-android`, from [The412Banner/proton-wine](https://github.com/The412Banner/proton-wine) |
| DXVK 2.4.1 (aarch64) | `dxvk-2.4.1-wow-aarch64-test.wcp` | `f660b5c6422b3f93cec65664f309718942e58581b5b96fa2902d55d63ce1db8a` | `components-v1`, from [doitsujin/dxvk](https://github.com/doitsujin/dxvk) |
| Turnip (Adreno 740) | `turnip-wow-scheduler-test.zip` | `02384f692735515f73aad8a544bc640abe3f35fe18e8df2e667be712c442e54c` | `components-v1`, Mesa with scheduler fix |
| Turnip (Adreno 8xx) | `turnip-V32-RP6sched.zip` | `05434d3c102563a4577f514410cc713494bcf900f52c2b935391adf87a76b77a` | [arusiasotto/wow-forever-a840](https://github.com/arusiasotto/wow-forever-a840), `turnip/gen8` v32 |

Licences of these binaries follow their upstream projects (LGPL/MIT as applicable). See `THIRD_PARTY_NOTICES` for the source-availability offer for copyleft components.

## Direct runtime dependencies

Versions come from `gradle/libs.versions.toml`; the exact set included in the APK comes from `app/build.gradle.kts`. Transitive dependencies are resolved by Gradle and are not enumerated here.

| Group | Dependency | Version |
|---|---|---|
| Network / compression | OkHttp | 5.1.0 |
| | Apache Commons Compress | 1.28.0 |
| | zstd-jni | 1.5.7-5 |
| | XZ (`org.tukaani:xz`) | 1.10 |
| Compose | Compose BOM | 2025.01.01 |
| | activity-compose | 1.10.0 |
| | material3 | 1.3.1 |
| | material3-window-size-class | 1.3.1 |
| | androidx.window | 1.0.0 |
| | navigation-compose | 2.8.6 |
| | Compose UI | 1.8.0-beta01 |
| | material-icons-extended | Compose BOM |
| | compose-settings-ui | 2.10.0 |
| AndroidX | core-ktx | 1.15.0 |
| | lifecycle-runtime-ktx | 2.8.7 |
| | datastore-preferences | 1.1.2 |
| Kotlin / logging | kotlinx-serialization-json | 1.8.0 |
| | kotlinx-coroutines-core | 1.10.2 |
| | Timber | 5.0.1 |
| DI | Hilt Android | 2.55 |
| | hilt-navigation-compose | 1.2.0 |

## Test-only dependencies

Not shipped in the APK: JUnit 4.13.2, Robolectric 4.14, Mockito 5.14.2, mockito-kotlin 5.3.1, MockK 1.13.5, AndroidX Test (runner 1.6.2, ext.junit 1.2.1, espresso-core 3.7.0), mockwebserver 5.1.0, `org.json` 20231013.

## Proprietary / source-withheld components (inherited from GameNative)

| Component | Location | Status |
|---|---|---|
| `libredirect.so` | `app/src/main/assets/redirect.tzst` | Proprietary; loaded only into Wine/Proton subprocesses via `LD_PRELOAD`; separable by deleting the asset |
| `libsteambootstrap.so` | `app/src/main/jniLibs/<abi>/` | Proprietary, source intentionally absent; separable by removing the library |

`THIRD_PARTY_NOTICES` documents the "mere aggregation" rationale and the offer of source for copyleft components. Do not redistribute either proprietary binary on its own and do not change their status without a licence review.

## Regeneration checklist

1. Change in `gradle/libs.versions.toml` or `app/build.gradle.kts` → update the tables above in the same commit.
2. Change in `tools/components.sha256` → update the runtime table with the new hash and source (only with explicit task approval).
3. Change in `THIRD_PARTY_NOTICES` → update the proprietary section.
4. Run `./gradlew assembleModernDebug` and note the date of the review in the commit message.
