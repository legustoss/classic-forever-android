# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** est un fork de WoW Forever for Android conçu spécialement pour jouer sur le **serveur privé Classic Forever**. Il lance **World of Warcraft: Forever** sur les handhelds Snapdragon comme une application Android ordinaire : touchez l'icône, appuyez sur Play, et le jeu démarre.

Il utilise le client **Windows ARM64** original de Blizzard : le jeu tourne donc nativement sur le CPU du téléphone. Wine traduit les appels Windows, et DXVK avec un pilote Turnip patché gère le rendu sur le GPU Adreno. Rien n'est émulé en x86.

> Projet communautaire non officiel. Non affilié ni soutenu par Blizzard Entertainment. Ce dépôt et ses releases ne contiennent **aucun** fichier de jeu de Blizzard.

> 💬 [Discord de Classic Forever](https://discord.gg/zBmq68jby) · 🌐 [Site web](http://classicforever.duckdns.org/)

---

## Ce que ce fork apporte

- **Patchleur de serveur privé intégré.** Port Kotlin de `classic-forever-linux.py` : il pointe le client vers votre hôte d'authentification via `-config BetaSuspendedTest.wtf` et remplace la clé publique en mémoire. Hôte et clé se configurent dans `classic-forever.properties` sans recompiler.
- **Sélecteur de langue.** Anglais, russe, espagnol, français, allemand, portugais (BR), chinois simplifié, japonais, coréen et « système par défaut ».
- **Logs sur l'appareil.** `logs/classic-forever.log` (patchleur) et `logs/app.log` (app) dans le dossier du jeu, à côté de `Data/`. Rotation à 2 Mo, lisibles sans adb.
- **Boutons communauté.** Accès direct au site web et à Discord depuis le launcher.
- **Interface pratique.** Réglages regroupés, version de l'app dans l'en-tête, actions fichiers et compte séparées.
- **Résolution native par défaut.** Les nouvelles installations démarrent à la résolution réelle de l'écran avec `RenderScale` à 50 %. Ces valeurs ne sont écrites **qu'au premier lancement** ; vos changements ultérieurs sont conservés.
- **Thème néon noir et blanc.**

---

## Appareils pris en charge

AYN Thor et Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) testés ; AYN Odin 2, OnePlus 15r, AYN Odin Portal, RedMagic 11 Pro, Poco F7 et les Samsung Galaxy S23/S24/S25 sont également compatibles. Prérequis : Snapdragon 8 (Adreno 7xx/8xx), Android 10+ 64 bits, environ **60 Go** libres pour les données et 4 Go internes, Wi-Fi.

---

## Installation

1. Téléchargez `Classic-Forever.apk` depuis la [dernière release](https://github.com/legustoss/classic-forever-android/releases/latest). Le paquet est `app.wowforever`. Si vous mettez à jour depuis une ancienne version de WoW Forever, utilisez la même clé de signature ; sinon désinstallez d'abord (les données restent sur la carte).
2. Ouvrez **Classic Forever**, accordez l'**accès à tous les fichiers** et appuyez sur **Download & Install**. Emplacement par défaut : `/storage/emulated/0/WoW Forever/` (modifiable avec **Change Install Location**). Le client ARM64 et les index CASC (~250 Mo) sont téléchargés avec vérification des hashes ; le reste arrive pendant le jeu (jusqu'à ~50 Go).
3. Facultatif depuis un PC : copiez `.build.info` (fichier caché de la racine d'installation Blizzard) et tout le dossier `Data/` à la racine du dossier de jeu. `Wow-ARM64.exe` n'est pas nécessaire. Autres options : carte SD, USB (MTP) ou `tools/sync_wow_to_device.sh`.
4. Depuis la bêta : **WoW Forever Has Launched → Upgrade Now** télécharge le nouveau client (~100 Mo), ajoute `wowf` à `.build.info` et conserve vos réglages de `_classic_beta_/WTF/Config.wtf`.

### Réglages du launcher

- **Resolution :** **Native** (résolution de l'appareil, par défaut), 720p, 900p ou 1080p.
- **Render scale :** 50 % (`SET RenderScale "0.5"`) pour les nouvelles installations.
- **Language :** langue du launcher, enregistrée entre les lancements.
- **Community :** boutons vers le site et le serveur Discord de Classic Forever.
- Maintenez **Start + Select + L2 + R2** (ou Back) sur l'écran de chargement pour revenir aux réglages.


---

## Serveur privé (Classic Forever)

Ce fork est fait pour le serveur **Classic Forever** : [site](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

Le patchleur (`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`) est un port de `classic-forever-linux.py`. Il tourne dans un thread de l'app et accède directement à `/proc/<pid>/mem`, sans root : il écrit `BetaSuspendedTest.wtf` avec votre portail, repère le processus du jeu et remplace la clé n° 8 du stockage de 12 clés lors de la connexion à un royaume ; il la réapplique si le client la restaure.

Valeurs par défaut : `auth.gpon.com.co` et sa clé. Pour votre serveur, créez `classic-forever.properties` à la racine du jeu :

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` fait 64 caractères hex de la clé **publique** (32 octets). Une valeur invalide retombe sur le défaut.

**Diagnostic :** `logs/classic-forever.log` dans le dossier du jeu et logcat avec le tag `ClassicForever`. `Waiting for Wow-ARM64.exe...` = processus introuvable (la liste des candidats est écrite toutes les 10 s) ; `Memory access denied` = SELinux bloque l'accès ; `Waiting for realm connection` après l'entrée dans le monde = mettez à jour `KNOWN_KEYS` depuis le script actuel. Si la première connexion échoue, réessayez **sans fermer le jeu**.

---

## Mise à jour, dépannage, compilation

Le mise à jour intégrée utilise les CDN publics de Blizzard sans PC. Si une nouvelle version existe, la carte de mise à jour apparaît (avec **Launch Anyway (Outdated)**). Solution de repli : `tools/sync_wow_to_device.sh`.

| Symptôme | Solution |
| :--- | :--- |
| *CAS system was unable to initialize* | `.build.info` ou `_forever_/.flavor.info` manquant. |
| Aucun royaume | Client obsolète : appuyez sur Update. |
| Clavier invisible | Arrêtez Gboard et rouvrez **Keyboard**. |
| Retour au launcher après le lancement | Vérifiez `_forever_/Errors/`. |
| Serveur privé ne connecte pas | Consultez `logs/classic-forever.log` : `Found game`, `Server public key written`, `READY`. |

Compilation : JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh`, puis `./gradlew assembleModernRelease` (ou `assembleModernDebug`).

---

## Crédits et licence

Basé sur **[GameNative](https://github.com/utkarshdalal/GameNative)** (GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, le bundle de la communauté **WoW Forever RP6** (Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)** et **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**. Détails dans [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES). Licence GPL-3.0, voir [`LICENSE`](LICENSE).
