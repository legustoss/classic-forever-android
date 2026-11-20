# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever**는 **Classic Forever 개인 서버**에서 플레이하기 위해 만들어진 WoW Forever for Android의 포크입니다. Snapdragon handheld에서 **World of Warcraft: Forever**를 일반 Android 앱처럼 실행합니다. 아이콘을 누르고 Play를 누르면 게임이 시작됩니다.

Blizzard의 자체 **Windows ARM64** 클라이언트를 사용하므로 게임은 휴대폰 CPU에서 네이티브로 실행됩니다. Wine이 Windows 호출을 변환하고, DXVK와 수정된 Turnip 드라이버가 Adreno GPU에서 렌더링합니다. x86 에뮬레이션은 없습니다.

> 비공식 커뮤니티 프로젝트입니다. Blizzard Entertainment와 관련이 없으며 지원도 받지 않습니다.  이 저장소와 릴리스에는 Blizzard 게임 파일이 **포함되지 않습니다**.

> 💬 [Classic Forever Discord](https://discord.gg/zBmq68jby) · 🌐 [프로젝트 사이트](http://classicforever.duckdns.org/)

---

## 이 포크의 추가 기능

- **내장 개인 서버 패처.** `classic-forever-linux.py`의 Kotlin 포트입니다. `-config BetaSuspendedTest.wtf`로 인증 호스트를 지정하고 메모리의 공개 키를 교체합니다. 호스트와 키는 APK 재빌드 없이 `classic-forever.properties`로 설정합니다.
- **언어 선택기.** 영어, 러시아어, 스페인어, 프랑스어, 독일어, 포르투갈어(BR), 간체 중국어, 일본어, 한국어 및 '시스템 기본값'.
- **기기 내 로그.** 게임 폴더의 `logs/classic-forever.log`(패처)와 `logs/app.log`(앱). 2 MB마다 회전하며 adb 없이 읽을 수 있습니다.
- **커뮤니티 버튼.** 런처에서 사이트와 Discord를 바로 엽니다.
- **실용적인 UI.** 설정을 용도별로 그룹화하고 버전을 상단에 표시하며 파일/계정 작업을 분리했습니다.
- **기본 네이티브 해상도.** 새 설치는 기기의 실제 해상도와 `RenderScale` 50%로 시작합니다. 이 값은 **첫 실행 시에만** 기록되며 이후 변경 사항은 유지됩니다.
- **흑백 네온 테마.**

---

## 지원 기기

AYN Thor와 Retroid Pocket 6(Snapdragon 8 Gen 2 / Adreno 740)에서 테스트되었습니다. AYN Odin 2, OnePlus 15r, AYN Odin Portal, RedMagic 11 Pro, Poco F7, Samsung Galaxy S23/S24/S25도 지원됩니다. 요구 사항: Snapdragon 8 시리즈(Adreno 7xx/8xx), Android 10 이상 64비트, 게임 데이터용 약 **60 GB**, 내부 4 GB, Wi-Fi.

---

## 설치

1. [최신 릴리스](https://github.com/legustoss/classic-forever-android/releases/latest)에서 `Classic-Forever.apk`를 다운로드하세요. 패키지명은 `app.wowforever`입니다. 이전 WoW Forever 빌드에서 업데이트할 때는 동일한 서명 키를 사용하세요. 키가 바뀌면 먼저 제거해야 합니다(게임 데이터는 유지됩니다).
2. **Classic Forever**를 열고 **모든 파일 접근**을 허용한 뒤 **Download & Install**을 누르세요. 기본 위치는 `/storage/emulated/0/WoW Forever/`이며 **Change Install Location**으로 변경할 수 있습니다. ARM64 클라이언트와 CASC 인덱스(약 250 MB)를 해시 검증과 함께 다운로드하고 나머지는 플레이 중 스트리밍됩니다(최대 약 50 GB).
3. PC에서 복사할 경우 `.build.info`(Blizzard 설치 루트의 숨김 파일)와 `Data/` 폴더 전체를 게임 루트에 넣으세요. `Wow-ARM64.exe`는 필요 없습니다. SD 카드, USB(MTP), `tools/sync_wow_to_device.sh`도 사용할 수 있습니다.
4. 베타에서 업그레이드: **WoW Forever Has Launched → Upgrade Now**가 새 클라이언트(약 100 MB)를 받고 `.build.info`에 `wowf`를 추가하며 `_classic_beta_/WTF/Config.wtf` 설정을 이어받습니다.

### 런처 설정

- **Resolution:** **네이티브**(기기 해상도, 기본값), 720p, 900p, 1080p.
- **Render scale:** 새 설치는 50%(`SET RenderScale "0.5"`).
- **Language:** 런처 언어로, 다시 실행해도 유지됩니다.
- **Community:** Classic Forever 사이트와 Discord 서버를 여는 버튼.
- 로딩 화면에서 **Start + Select + L2 + R2**(또는 Back)를 길게 누르면 설정으로 돌아갑니다.


---

## 개인 서버(Classic Forever)

이 포크는 **Classic Forever** 서버용입니다: [사이트](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

패처(`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`)는 `classic-forever-linux.py`의 포트입니다. 앱 내부 스레드로 실행되며 `/proc/<pid>/mem`에 직접 접근합니다(root 불필요). 지정한 포털이 담긴 `BetaSuspendedTest.wtf`를 만들고 게임 프로세스를 찾아 realm 접속 시 12개 키 저장소의 8번째를 서버 공개 키로 교체합니다. 클라이언트가 원래 키로 되돌리면 다시 적용합니다.

기본값은 `auth.gpon.com.co`와 해당 키입니다. 자체 서버를 쓰려면 게임 루트에 `classic-forever.properties`를 만드세요:

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key`는 **공개 키**의 16진수 64자(32바이트)입니다. 잘못된 값은 기본값으로 대체됩니다.

**진단:** 게임 폴더의 `logs/classic-forever.log`와 `ClassicForever` 태그의 logcat에 기록됩니다. `Waiting for Wow-ARM64.exe...`는 프로세스를 찾지 못한 경우(10초마다 후보 기록), `Memory access denied`는 SELinux 차단, 월드 진입 후에도 `Waiting for realm connection`이 계속되면 최신 스크립트에서 `KNOWN_KEYS`를 업데이트하세요. 첫 realm 로그인이 실패하면 게임을 **닫지 말고** 다시 시도하세요.

---

## 업데이트, 문제 해결, 빌드

내장 업데이터는 Blizzard 공개 CDN을 직접 사용합니다(PC 불필요). 새 빌드가 있으면 업데이트 카드가 나타납니다(**Launch Anyway (Outdated)** 선택 가능). 대안은 `tools/sync_wow_to_device.sh`입니다.

| 증상 | 해결 방법 |
| :--- | :--- |
| *CAS system was unable to initialize* | `.build.info` 또는 `_forever_/.flavor.info`가 없습니다. |
| realm 목록이 없음 | 클라이언트가 오래됨 — Update를 누르세요. |
| 키보드가 나타나지 않음 | Gboard를 강제 종료한 뒤 **Keyboard**를 다시 여세요. |
| 시작 후 런처로 돌아감 | 기기의 `_forever_/Errors/`를 확인하세요. |
| 개인 서버가 연결되지 않음 | `logs/classic-forever.log`의 `Found game`, `Server public key written`, `READY`를 확인하세요. |

빌드: JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh` 실행 후 `./gradlew assembleModernRelease`(또는 `assembleModernDebug`).

---

## 감사의 말과 라이선스

**[GameNative](https://github.com/utkarshdalal/GameNative)**(GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, **WoW Forever RP6** 커뮤니티 컴포넌트(Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)**, **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**의 작업을 기반으로 합니다. 전체 목록은 [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES)에 있습니다. 라이선스는 GPL-3.0이며 [`LICENSE`](LICENSE)를 참조하세요.
