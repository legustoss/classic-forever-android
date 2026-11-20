# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** es un fork de WoW Forever for Android creado específicamente para jugar en el **servidor privado Classic Forever**. Ejecuta **World of Warcraft: Forever** en handhelds Snapdragon como una app Android normal: tocas el icono, pulsas Play y el juego arranca.

Usa el cliente **Windows ARM64** original de Blizzard, así que el juego corre de forma nativa en la CPU del teléfono. Wine traduce las llamadas de Windows y DXVK junto con un controlador Turnip parcheado renderiza en la GPU Adreno. Nada se emula en x86.

> Proyecto no oficial. No afiliado ni respaldado por Blizzard Entertainment. Este repositorio y sus releases **no contienen** archivos del juego de Blizzard.

> 💬 [Discord de Classic Forever](https://discord.gg/zBmq68jby) · 🌐 [Sitio web](http://classicforever.duckdns.org/)

---

## Qué añade este fork

- **Parcheador de servidor privado integrado.** Port Kotlin de `classic-forever-linux.py`: apunta el cliente a tu host de autenticación mediante `-config BetaSuspendedTest.wtf` y reemplaza la clave pública en memoria. Host y clave se configuran en `classic-forever.properties` sin recompilar.
- **Selector de idioma.** Inglés, ruso, español, francés, alemán, portugués (BR), chino simplificado, japonés, coreano y «predeterminado del sistema».
- **Logs en el dispositivo.** `logs/classic-forever.log` (parcheador) y `logs/app.log` (app) en la carpeta del juego, junto a `Data/`. Rotación a 2 MB, legibles sin adb.
- **Botones de comunidad.** Accesos directos al sitio web y Discord en el launcher.
- **Interfaz práctica.** Ajustes agrupados, versión de la app en la cabecera, acciones de archivos y cuenta separadas.
- **Resolución nativa por defecto.** Las instalaciones nuevas arrancan a la resolución real del dispositivo con `RenderScale` al 50%. Estos valores se escriben **solo en el primer inicio**; los cambios posteriores se conservan.
- **Tema neón blanco y negro.**

---

## Dispositivos soportados

AYN Thor y Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) probados; AYN Odin 2, OnePlus 15r, AYN Odin Portal, RedMagic 11 Pro, Poco F7 y la serie Samsung Galaxy S23/S24/S25 también son compatibles. Requisitos: Snapdragon 8 (Adreno 7xx/8xx), Android 10+ de 64 bits, unos **60 GB** libres para los datos y 4 GB internos, y Wi-Fi.

---

## Instalación

1. Descarga `Classic-Forever.apk` del [último release](https://github.com/legustoss/classic-forever-android/releases/latest). El paquete es `app.wowforever`, compatible con GameNative y Winlator. Si actualizas desde una build anterior de WoW Forever, usa la misma clave de firma; si cambia, desinstala primero (los datos se conservan).
2. Abre **Classic Forever**, concede **Acceso a todos los archivos** y pulsa **Download & Install**. La ubicación por defecto es `/storage/emulated/0/WoW Forever/`; cámbiala con **Change Install Location**. La app descarga el cliente ARM64 y los índices CASC (unos 250 MB) verificando hashes; el resto se descarga mientras juegas (hasta ~50 GB).
3. Opcionalmente copia desde el PC: pon `.build.info` (archivo oculto de la raíz de instalación de Blizzard) y toda la carpeta `Data/` en la raíz del juego. No necesitas `Wow-ARM64.exe`. También puedes usar SD, USB (MTP) o `tools/sync_wow_to_device.sh`.
4. Desde la beta: **WoW Forever Has Launched → Upgrade Now** descarga el cliente nuevo (~100 MB), añade `wowf` a `.build.info` y conserva tus ajustes.

### Ajustes del launcher

- **Resolution:** **Native** (resolución real, valor por defecto), 720p, 900p o 1080p.
- **Render scale:** 50% (`SET RenderScale "0.5"`) en instalaciones nuevas.
- **Language:** idioma del launcher, guardado entre sesiones.
- **Community:** botones Website y Discord del servidor Classic Forever.
- Mantén **Start + Select + L2 + R2** (o Back) en la pantalla de carga para volver a los ajustes.


---

## Servidor privado (Classic Forever)

Este fork está hecho para el servidor **Classic Forever**: [web](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

El parcheador (`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`) es un port de `classic-forever-linux.py`. Trabaja como hilo dentro de la app y accede a `/proc/<pid>/mem` directamente, sin root: crea `BetaSuspendedTest.wtf` con tu portal, espera al proceso del juego y sustituye la clave 8 del almacén de 12 claves cuando entras a un reino; la reaplica si el cliente la restaura.

Por defecto usa `auth.gpon.com.co` y su clave. Para tu servidor, crea `classic-forever.properties` en la raíz del juego:

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` son 64 caracteres hex de la clave **pública** (32 bytes). Si el valor es inválido se usa el predeterminado.

**Diagnóstico:** `logs/classic-forever.log` en la carpeta del juego y logcat con etiqueta `ClassicForever`. `Waiting for Wow-ARM64.exe...` indica que no encontró el proceso (cada 10 s lista candidatos); `Memory access denied` significa que SELinux bloquea el acceso; `Waiting for realm connection` tras entrar al mundo indica que hay que actualizar `KNOWN_KEYS`. Si el primer intento de entrada falla, repite **sin cerrar el juego**.

---

## Actualización, problemas y compilación

El actualizador usa los CDN públicos de Blizzard sin PC. Si hay una build nueva aparece la tarjeta de actualización (con **Launch Anyway (Outdated)**). Alternativa: `tools/sync_wow_to_device.sh`.

| Síntoma | Solución |
| :--- | :--- |
| *CAS system was unable to initialize* | Falta `.build.info` o `_forever_/.flavor.info`. |
| Sin realms | Cliente desactualizado: pulsa Update. |
| Teclado invisible | Detén Gboard y vuelve a abrir **Keyboard**. |
| Vuelve al launcher tras iniciar | Revisa `_forever_/Errors/`. |
| Servidor privado no conecta | Mira `logs/classic-forever.log`: `Found game`, `Server public key written`, `READY`. |

Compilación: JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh` y `./gradlew assembleModernRelease` (o `assembleModernDebug`).

---

## Créditos y licencia

Basado en **[GameNative](https://github.com/utkarshdalal/GameNative)** (GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, el bundle de la comunidad **WoW Forever RP6** (Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)** y **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**. Detalles en [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES). Licencia GPL-3.0, véase [`LICENSE`](LICENSE).
