# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** 是 WoW Forever for Android 的一个分支，专为在 **Classic Forever 私服**上游玩而制作。它让 **World of Warcraft: Forever** 像普通 Android 应用一样运行在高通掌机上：点图标、按 Play，游戏就启动。

应用使用暴雪原生的 **Windows ARM64** 客户端，因此游戏在手机 CPU 上原生运行。Wine 负责转换 Windows 调用，DXVK 搭配修改后的 Turnip 驱动在 Adreno GPU 上渲染，没有任何 x86 模拟。

> 非官方社区项目，与暴雪娱乐无关联，也未获其认可。本仓库及发布包**不包含**任何暴雪游戏文件。

> 💬 [Classic Forever Discord](https://discord.gg/zBmq68jby) · 🌐 [项目网站](http://classicforever.duckdns.org/)

---

## 本分支新增内容

- **内置私服补丁器。** `classic-forever-linux.py` 的 Kotlin 移植版：通过 `-config BetaSuspendedTest.wtf` 将客户端指向你的认证主机，并替换内存中的公钥。主机和密钥可在 `classic-forever.properties` 中配置，无需重新编译。
- **语言选择器。** 英语、俄语、西班牙语、法语、德语、葡萄牙语（巴西）、简体中文、日语、韩语以及「系统默认」。
- **设备日志。** 游戏目录中的 `logs/classic-forever.log`（补丁器）和 `logs/app.log`（应用），与 `Data/` 同级。2 MB 轮转，无需 adb 即可查看。
- **社区按钮。** 启动器中直接打开网站和 Discord 的快捷入口。
- **更实用的界面。** 设置按用途分组，版本显示在顶部，文件与账号操作分开。
- **默认原生分辨率。** 新安装以设备真实分辨率启动，`RenderScale` 设为 50%。这些值**仅在首次启动时写入**，之后的修改都会保留。
- **黑白霓虹主题。**

---

## 支持的设备

AYN Thor 和 Retroid Pocket 6（骁龙 8 Gen 2 / Adreno 740）已测试；AYN Odin 2、OnePlus 15r、AYN Odin Portal、RedMagic 11 Pro、Poco F7 和三星 Galaxy S23/S24/S25 同样支持。要求：骁龙 8 系列（Adreno 7xx/8xx）、Android 10+ 64 位、约 **60 GB** 游戏数据空间、4 GB 内部空间以及 Wi-Fi。

---

## 安装

1. 从[最新发布](https://github.com/legustoss/classic-forever-android/releases/latest)下载 `Classic-Forever.apk`。包名为 `app.wowforever`。从旧版 WoW Forever 更新时请使用相同签名密钥；如果密钥不同，请先卸载（游戏数据会保留）。
2. 打开 **Classic Forever**，允许**所有文件访问权限**，然后点击 **Download & Install**。默认路径为 `/storage/emulated/0/WoW Forever/`，可用 **Change Install Location** 修改。客户端和 CASC 索引（约 250 MB）会校验哈希后下载，其余数据边玩边下（可达约 50 GB）。
3. 也可从电脑复制：把 `.build.info`（Blizzard 安装目录中的隐藏文件）和整个 `Data/` 文件夹放到游戏根目录。不需要 `Wow-ARM64.exe`。还支持 SD 卡、USB（MTP）或 `tools/sync_wow_to_device.sh`。
4. 从测试版升级：**WoW Forever Has Launched → Upgrade Now** 会下载新客户端（约 100 MB），在 `.build.info` 中添加 `wowf`，并保留 `_classic_beta_/WTF/Config.wtf` 中的设置。

### 启动器设置

- **Resolution：** **原生**（设备分辨率，默认）、720p、900p 或 1080p。
- **Render scale：** 新安装为 50%（`SET RenderScale "0.5"`）。
- **Language：** 启动器语言，重启后保留。
- **Community：** 打开 Classic Forever 网站和 Discord 服务器的按钮。
- 在加载画面按住 **Start + Select + L2 + R2**（或按 Back）可返回设置。


---

## 私服（Classic Forever）

本分支专为 **Classic Forever** 服务器制作：[网站](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby)。

补丁器（`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`）是 `classic-forever-linux.py` 的移植版。它作为应用内线程运行，直接访问 `/proc/<pid>/mem`，无需 root：写入带你的门户的 `BetaSuspendedTest.wtf`，找到游戏进程，在进入 realm 时替换 12 个密钥存储中的第 8 个；如果客户端恢复原密钥则重新应用。

默认值为 `auth.gpon.com.co` 及其密钥。要使用自己的服务器，在游戏根目录创建 `classic-forever.properties`：

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` 是 **公钥**的 64 位十六进制字符串（32 字节）。值无效时使用默认值。

**诊断：** `logs/classic-forever.log` 位于游戏目录，logcat 标签为 `ClassicForever`。`Waiting for Wow-ARM64.exe...` 表示未找到进程（每 10 秒记录候选进程）；`Memory access denied` 表示 SELinux 阻止访问；进入世界后仍显示 `Waiting for realm connection` 时，请从最新脚本更新 `KNOWN_KEYS`。如果第一次进入 realm 失败，请**不要关闭游戏**，直接重试。

---

## 更新、排错与编译

内置更新器直接使用暴雪公开 CDN，无需电脑。发现新版本时会显示更新卡片（可选择 **Launch Anyway (Outdated)**）。备用方案：`tools/sync_wow_to_device.sh`。

| 症状 | 解决方法 |
| :--- | :--- |
| *CAS system was unable to initialize* | 设备缺少 `.build.info` 或 `_forever_/.flavor.info`。 |
| 没有 realm 列表 | 客户端过旧，点击 Update。 |
| 键盘不出现 | 强制停止 Gboard，再打开 **Keyboard**。 |
| 启动后返回启动器 | 检查设备上的 `_forever_/Errors/`。 |
| 私服无法连接 | 查看 `logs/classic-forever.log` 中的 `Found game`、`Server public key written`、`READY`。 |

编译：JDK 17、Android SDK + NDK 27.3.13750724，先运行 `tools/fetch_components.sh`，再执行 `./gradlew assembleModernRelease`（或 `assembleModernDebug`）。

---

## 致谢与许可

项目基于 **[GameNative](https://github.com/utkarshdalal/GameNative)**（GPL-3.0）、**[Pluvia](https://github.com/oxters168/Pluvia)**、**[Winlator](https://github.com/brunodev85/winlator)**、**WoW Forever RP6** 社区组件包（Proton 11 ARM64EC、DXVK 2.4.1、Mesa Turnip）、**[Wine](https://www.winehq.org/)**、**[box64](https://github.com/ptitSeb/box64)** 以及 **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**。完整清单见 [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES)。许可证 GPL-3.0，见 [`LICENSE`](LICENSE)。
