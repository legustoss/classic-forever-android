# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** は **Classic Forever プライベートサーバー**でのプレイ専用に作られた WoW Forever for Android のフォークです。Snapdragon 搭載ハンドヘルドで **World of Warcraft: Forever** を通常の Android アプリとして起動します。アイコンをタップして Play を押すだけです。

Blizzard 純正の **Windows ARM64** クライアントを使うため、ゲームは端末 CPU 上でネイティブに動作します。Wine が Windows コールを変換し、DXVK と調整済み Turnip ドライバが Adreno GPU で描画します。x86 エミュレーションはありません。

> 非公式コミュニティプロジェクトです。Blizzard Entertainment とは無関係で、支援も受けていません。このリポジトリとリリースに Blizzard のゲームファイルは**含まれていません**。

> 💬 [Classic Forever Discord](https://discord.gg/zBmq68jby) · 🌐 [プロジェクトサイト](http://classicforever.duckdns.org/)

---

## このフォークの追加機能

- **プライベートレルムパッチャー内蔵.** `classic-forever-linux.py` の Kotlin 移植版。`-config BetaSuspendedTest.wtf` で認証ホストを指定し、メモリ内の公開鍵を差し替えます。ホストと鍵は `classic-forever.properties` で再ビルドなしに変更できます。
- **言語選択.** 英語、ロシア語、スペイン語、フランス語、ドイツ語、ポルトガル語（BR）、簡体字中国語、日本語、韓国語、「システム既定」。
- **端末内ログ.** ゲームフォルダの `logs/classic-forever.log`（パッチャー）と `logs/app.log`（アプリ）。2 MB でローテーションし、adb なしで読めます。
- **コミュニティボタン.** ランチャーからサイトと Discord を直接開けます。
- **実用的な UI.** 設定を用途別にグループ化し、バージョンをヘッダーに表示、ファイル操作とアカウント操作を分離。
- **既定でネイティブ解像度.** 新規インストールは端末の実解像度、`RenderScale` は 50% で起動します。これらは**初回起動時のみ**書き込まれ、以降の変更は保持されます。
- **モノクロネオンテーマ.**

---

## 対応デバイス

AYN Thor と Retroid Pocket 6（Snapdragon 8 Gen 2 / Adreno 740）で動作確認済み。AYN Odin 2、OnePlus 15r、AYN Odin Portal、RedMagic 11 Pro、Poco F7、Samsung Galaxy S23/S24/S25 も対応。要件：Snapdragon 8 シリーズ（Adreno 7xx/8xx）、Android 10 以降 64 ビット、ゲームデータ用に約 **60 GB**、内部 4 GB、Wi-Fi。

---

## インストール

1. [最新リリース](https://github.com/legustoss/classic-forever-android/releases/latest)から `Classic-Forever.apk` を入手してください。パッケージ名は `app.wowforever` です。旧バージョンから更新する場合は同じ署名鍵を使ってください。鍵が変わる場合は先にアンインストールしてください（ゲームデータは残ります）。
2. **Classic Forever** を開き、**すべてのファイルへのアクセス**を許可して **Download & Install** を押します。既定の場所は `/storage/emulated/0/WoW Forever/` で、**Change Install Location** で変更できます。ARM64 クライアントと CASC インデックス（約 250 MB）をハッシュ検証付きでダウンロードし、残りはプレイ中に streaming されます（最大約 50 GB）。
3. PC からコピーする場合は、`.build.info`（Blizzard インストールルートの隠しファイル）と `Data/` フォルダ全体をゲームルートに置きます。`Wow-ARM64.exe` は不要です。SD カード、USB（MTP）、`tools/sync_wow_to_device.sh` も使えます。
4. ベータから更新する場合は **WoW Forever Has Launched → Upgrade Now** で新しいクライアント（約 100 MB）をダウンロードし、`.build.info` に `wowf` を追加して `_classic_beta_/WTF/Config.wtf` の設定を引き継ぎます。

### ランチャー設定

- **Resolution：** **ネイティブ**（端末解像度、既定）、720p、900p、1080p。
- **Render scale：** 新規インストールでは 50%（`SET RenderScale "0.5"`）。
- **Language：** ランチャーの言語。再起動後も保持されます。
- **Community：** Classic Forever のサイトと Discord サーバーを開くボタン。
- ロード画面で **Start + Select + L2 + R2**（または Back）を押し続けると設定に戻れます。


---

## プライベートサーバー（Classic Forever）

このフォークは **Classic Forever** サーバー用です：[サイト](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby)。

パッチャー（`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`）は `classic-forever-linux.py` の移植版です。アプリ内のスレッドとして動作し、`/proc/<pid>/mem` に直接アクセスします（root 不要）。自分のポータルを書いた `BetaSuspendedTest.wtf` を用意し、ゲームプロセスを見つけて、realm 接続時に 12 個の鍵ストアの 8 番目をサーバーの公開鍵に差し替えます。元に戻された場合は再適用します。

既定値は `auth.gpon.com.co` とその鍵です。自分のサーバーを使う場合はゲームルートに `classic-forever.properties` を作成します：

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` は **公開鍵**の 16 進数 64 文字（32 バイト）です。不正な値は既定値にフォールバックします。

**診断：** ゲームフォルダの `logs/classic-forever.log` と、タグ `ClassicForever` の logcat に出力されます。`Waiting for Wow-ARM64.exe...` はプロセス未検出（10 秒ごとに候補を記録）、`Memory access denied` は SELinux によるブロック、ワールドに入っても `Waiting for realm connection` が続く場合は最新スクリプトから `KNOWN_KEYS` を更新してください。最初の realm ログインに失敗した場合は、ゲームを**閉じずに**再試行してください。

---

## 更新・トラブルシューティング・ビルド

アプリ内アップデータは Blizzard の公開 CDN を直接利用します（PC 不要）。新しいビルドがあると更新カードが表示されます（**Launch Anyway (Outdated)** も選択可）。代替手段は `tools/sync_wow_to_device.sh` です。

| 症状 | 対処 |
| :--- | :--- |
| *CAS system was unable to initialize* | `.build.info` または `_forever_/.flavor.info` がありません。 |
| realm が表示されない | クライアントが古いので Update を押してください。 |
| キーボードが出ない | Gboard を強制停止し、**Keyboard** を再度開いてください。 |
| 起動後にランチャーへ戻る | 端末の `_forever_/Errors/` を確認してください。 |
| 私服に接続しない | `logs/classic-forever.log` の `Found game`、`Server public key written`、`READY` を確認。 |

ビルド：JDK 17、Android SDK + NDK 27.3.13750724、`tools/fetch_components.sh` を実行後、`./gradlew assembleModernRelease`（または `assembleModernDebug`）。

---

## クレジットとライセンス

**[GameNative](https://github.com/utkarshdalal/GameNative)**（GPL-3.0）、**[Pluvia](https://github.com/oxters168/Pluvia)**、**[Winlator](https://github.com/brunodev85/winlator)**、**WoW Forever RP6** コミュニティ製コンポーネント（Proton 11 ARM64EC、DXVK 2.4.1、Mesa Turnip）、**[Wine](https://www.winehq.org/)**、**[box64](https://github.com/ptitSeb/box64)**、**[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)** の成果に基づきます。詳細は [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES)。ライセンスは GPL-3.0、[`LICENSE`](LICENSE) を参照。
