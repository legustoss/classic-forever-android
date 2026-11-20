# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** — это форк WoW Forever for Android, созданный специально для игры на **приватном сервере Classic Forever**. Приложение запускает **World of Warcraft: Forever** на Snapdragon-устройствах как обычное Android-приложение: тапаете иконку, жмёте Play — игра работает.

Используется родной **Windows ARM64**-клиент Blizzard, поэтому игра выполняется нативно на CPU телефона. Wine переводит Windows-вызовы, а DXVK и пропатченный Turnip отрисовывают картинку на Adreno. Ничего не эмулируется в x86.

> Неофициальный проект. Не связан и не поддерживается Blizzard Entertainment. Репозиторий и релизы **не содержат** файлов игры Blizzard.

> 💬 [Discord Classic Forever](https://discord.gg/zBmq68jby) · 🌐 [Сайт проекта](http://classicforever.duckdns.org/)

---

## Что добавил этот форк

- **Встроенный патчер приватного сервера.** Порт `classic-forever-linux.py` на Kotlin: прописывает ваш auth-портал через `-config BetaSuspendedTest.wtf` и заменяет публичный ключ в памяти клиента. Хост и ключ задаются файлом `classic-forever.properties` без пересборки APK.
- **Выбор языка интерфейса.** Английский, русский, испанский, французский, немецкий, португальский (BR), упрощённый китайский, японский, корейский и «системный». Выбор сохраняется.
- **Логи на устройстве.** `logs/classic-forever.log` (патчер) и `logs/app.log` (приложение) в папке игры рядом с `Data/`, ротация по 2 МБ, читаются файловым менеджером — adb не нужен.
- **Кнопки сообщества.** Ссылки на сайт и Discord прямо на экране лаунчера.
- **Удобная компоновка.** Настройки сгруппированы, версия приложения в шапке, действия с файлами и аккаунтом разделены.
- **Родное разрешение по умолчанию.** Новые установки стартуют в реальном разрешении экрана с `RenderScale` 50%. Эти значения пишутся **только при первом запуске**; последующие изменения в игре сохраняются.
- **Чёрно-белая неоновая тема** лаунчера и заставки.

Остальное — тот же проверенный стек: прямой запуск ARM64, in-app обновление игры.

---

## Поддерживаемые устройства

| Устройство | Статус |
| :--- | :--- |
| AYN Thor (Snapdragon 8 Gen 2 / Adreno 740) | Проверено, игра работает с геймпадом |
| Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) | Проверено исходным community-набором |
| AYN Odin 2 / Mini (Snapdragon 8 Gen 2) | Должно работать |
| OnePlus 15r (Snapdragon 8 Gen 5 / Adreno 829) | Проверено в игре (Nathangarl) |
| AYN Odin Portal (Snapdragon 8 Elite / Adreno 830) | Поддерживается через Turnip v32 |
| RedMagic 11 Pro (Snapdragon 8 Elite / Adreno 840) | Проверено в игре (arusiasotto) |
| Poco F7 (Snapdragon 8s Gen 4 / Adreno 825) | Проверено в игре (gusebus & legustoss) |
| Samsung Galaxy S23/S24/S25 / Z Fold | Поддерживается (UBWC-оптимизация) |

Требования: Snapdragon 8 серии (Adreno 7xx/8xx), Android 10+, 64-bit, около **60 ГБ** на данные игры и 4 ГБ внутри, Wi-Fi.

---

## Установка

### 1. Приложение

Скачайте `Classic-Forever.apk` из [последнего релиза](https://github.com/legustoss/classic-forever-android/releases/latest). Пакет — `app.wowforever`, приложение уживается с GameNative и Winlator.


### 2. Игра

Откройте **Classic Forever**, разрешите **доступ ко всем файлам**. Лаунчер покажет **Install World of Warcraft** с папкой по умолчанию `/storage/emulated/0/WoW Forever/`. Кнопка **Change Install Location** меняет папку, затем **Download & Install**.

Приложение скачает ARM64-клиент и индексы CASC (около 250 МБ) с CDN Blizzard с проверкой хешей. Остальные данные подгружаются во время игры и вырастают примерно до 50 ГБ.

Копирование с ПК: положите в корень игры `.build.info` (скрытый файл из корня установки Blizzard) и всю папку `Data/`. `Wow-ARM64.exe` копировать не нужно. Можно также залить файлы через SD-карту, USB (MTP) или ADB:

```bash
WOW_SRC="/Applications/World of Warcraft" tools/sync_wow_to_device.sh
```

Обновление с беты: **WoW Forever Has Launched → Upgrade Now** скачает новый клиент (около 100 МБ), добавит `wowf` в `.build.info` и перенесёт настройки из `_classic_beta_/WTF/Config.wtf`. Архивы `Data/` используются повторно.

### 3. Запуск и настройки

- **Setup screen:** зажмите **Start + Select + L2 + R2** (или Back) на заставке, чтобы вернуться в настройки.
- **Resolution:** **Native** (родное разрешение, по умолчанию), 720p, 900p, 1080p.
- **Render scale:** новые установки использую 50% (`SET RenderScale "0.5"`).
- **Language:** язык лаунчера; сохраняется между запусками.
- **Community:** кнопки Website и Discord открывают сайт и дискорд сервера Classic Forever.
- **Add-on:** выберите `.zip` кнопкой **Add-on** — он установится в `_forever_/Interface/AddOns`.

**Управление и вход:** геймпад работает через режим WoW; Back открывает боковую панель; клавиатура появляется на нижнем экране dual-screen устройств. Учётные данные хранятся в Keystore, при запуске создаётся `_forever_/login.txt`.

---

## Приватный сервер (Classic Forever)

Форк предназначен для сервера **Classic Forever**: [сайт](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

Логика `classic-forever-linux.py` портирована на Kotlin (`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`). Патчер работает фоновым потоком внутри приложения и обращается к `/proc/<pid>/mem` напрямую — без root и без патчей файлам игры. При каждом запуске он:

1. Создаёт `_forever_/WTF/BetaSuspendedTest.wtf` из `Config.wtf` с `SET portal` на ваш сервер и передаёт `-config BetaSuspendedTest.wtf` клиенту.
2. Находит процесс игры и хранилище из 12 ключей в памяти, заменяет ключ группы 8 на публичный ключ сервера.
3. Повторно применяет ключ, если клиент его восстановил, и останавливается при выходе.

**Диагностика:** `logs/classic-forever.log` в корне игры и logcat с тегом `ClassicForever`. `Waiting for Wow-ARM64.exe...` — процесс не найден (каждые 10 с пишутся кандидаты); `Memory access denied` — SELinux блокирует доступ; `Waiting for realm connection` — обновите `KNOWN_KEYS` из актуального скрипта. Если первый вход в мир не удался — повторите, не закрывая игру.

---

## Обновление игры, устранение неполадок, сборка

Встроенный обновлятель работает с CDN Blizzard без ПК. При наличии новой сборки показывается карточка обновления с опцией **Launch Anyway (Outdated)**. Резервный вариант — `tools/sync_wow_to_device.sh`.

| Симптом | Решение |
| :--- | :--- |
| *CAS system was unable to initialize* | Нет `.build.info` или `_forever_/.flavor.info`. |
| Нет realm-серверов | Клиент устарел — нажмите Update. |
| Клавиатура не появляется | Остановите Gboard и откройте **Keyboard** заново. |
| Возврат в лаунчер после запуска | Проверьте `_forever_/Errors/`. |
| Сервер не подключается | Смотрите `logs/classic-forever.log`: `Found game`, `Server public key written`, `READY`. |

Сборка: JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh`, затем `./gradlew assembleModernRelease` или `assembleModernDebug`.

---

## Благодарности и лицензия

Проект объединяет работу **[GameNative](https://github.com/utkarshdalal/GameNative)** (GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, сообщества **WoW Forever RP6** (Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)** и **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**. Полный список — в [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES).

Лицензия GPL-3.0, см. [`LICENSE`](LICENSE).
