# Classic Forever

[English](README.md) · [Русский](README_RU.md) · [Español](README_ES.md) · [Deutsch](README_DE.md) · [Français](README_FR.md) · [Português (BR)](README_PT_BR.md) · [简体中文](README_ZH_CN.md) · [日本語](README_JA.md) · [한국어](README_KO.md)

**Classic Forever** é um fork do WoW Forever for Android feito especificamente para jogar no **servidor privado Classic Forever**. Ele roda o **World of Warcraft: Forever** em handhelds Snapdragon como um app Android comum: toque no ícone, pressione Play e o jogo começa.

Usa o cliente **Windows ARM64** original da Blizzard, então o jogo roda nativamente na CPU do celular. Wine traduz as chamadas Windows, e DXVK com um driver Turnip modificado faz a renderização na GPU Adreno. Nada é emulado em x86.

> Projeto comunitário não oficial. Não afiliado nem apoiado pela Blizzard Entertainment. Este repositório e seus releases **não contêm** arquivos do jogo da Blizzard.

> 💬 [Discord do Classic Forever](https://discord.gg/zBmq68jby) · 🌐 [Site do projeto](http://classicforever.duckdns.org/)

---

## O que este fork adiciona

- **Patchador de servidor privado integrado.** Port Kotlin de `classic-forever-linux.py`: aponta o cliente para seu host de autenticação via `-config BetaSuspendedTest.wtf` e substitui a chave pública na memória. Host e chave são configurados em `classic-forever.properties` sem recompilar.
- **Seletor de idioma.** Inglês, russo, espanhol, francês, alemão, português (BR), chinês simplificado, japonês, coreano e «padrão do sistema».
- **Logs no dispositivo.** `logs/classic-forever.log` (patchador) e `logs/app.log` (app) na pasta do jogo, ao lado de `Data/`. Rotação em 2 MB, legíveis sem adb.
- **Botões da comunidade.** Atalhos para o site e o Discord direto no launcher.
- **Interface prática.** Ajustes agrupados, versão do app no cabeçalho, ações de arquivo e conta separadas.
- **Resolução nativa por padrão.** Instalações novas começam na resolução real da tela com `RenderScale` em 50%. Esses valores são gravados **apenas no primeiro launch**; mudanças posteriores são preservadas.
- **Tema neon preto e branco.**

---

## Dispositivos suportados

AYN Thor e Retroid Pocket 6 (Snapdragon 8 Gen 2 / Adreno 740) testados; AYN Odin 2, OnePlus 15r, AYN Odin Portal, RedMagic 11 Pro, Poco F7 e Samsung Galaxy S23/S24/S25 também são compatíveis. Requisitos: Snapdragon 8 (Adreno 7xx/8xx), Android 10+ 64 bits, cerca de **60 GB** livres para os dados e 4 GB internos, Wi-Fi.

---

## Instalação

1. Baixe `Classic-Forever.apk` do [último release](https://github.com/legustoss/classic-forever-android/releases/latest). O pacote é `app.wowforever`. Ao atualizar de uma build antiga do WoW Forever, use a mesma chave de assinatura; se mudar, desinstale primeiro (os dados na pasta ficam).
2. Abra **Classic Forever**, conceda **Acesso a todos os arquivos** e toque em **Download & Install**. O padrão é `/storage/emulated/0/WoW Forever/` (mude com **Change Install Location**). O cliente ARM64 e os índices CASC (~250 MB) são baixados com verificação de hash; o resto vem durante o jogo (até ~50 GB).
3. Opcional pelo PC: copie `.build.info` (arquivo oculto da raiz da instalação da Blizzard) e a pasta `Data/` inteira para a raiz do jogo. `Wow-ARM64.exe` não é necessário. Também dá pelo cartão SD, USB (MTP) ou `tools/sync_wow_to_device.sh`.
4. Da beta: **WoW Forever Has Launched → Upgrade Now** baixa o cliente novo (~100 MB), adiciona `wowf` ao `.build.info` e mantém suas configurações de `_classic_beta_/WTF/Config.wtf`.

### Ajustes do launcher

- **Resolution:** **Nativa** (resolução do dispositivo, padrão), 720p, 900p ou 1080p.
- **Render scale:** 50% (`SET RenderScale "0.5"`) em instalações novas.
- **Language:** idioma do launcher, salvo entre execuções.
- **Community:** botões para o site e o servidor Discord do Classic Forever.
- Segure **Start + Select + L2 + R2** (ou Back) na tela de carregamento para voltar aos ajustes.


---

## Servidor privado (Classic Forever)

Este fork é feito para o servidor **Classic Forever**: [site](http://classicforever.duckdns.org/) · [Discord](https://discord.gg/zBmq68jby).

O patchador (`app/src/main/java/app/gamenative/utils/ClassicForeverPatcher.kt`) é um port de `classic-forever-linux.py`. Roda como thread dentro do app e acessa `/proc/<pid>/mem` diretamente, sem root: escreve o `BetaSuspendedTest.wtf` com seu portal, encontra o processo do jogo e substitui a chave 8 do storage de 12 chaves ao entrar em um reino; reaplica se o cliente a restaurar.

Padrões: `auth.gpon.com.co` e sua chave. Para seu servidor, crie `classic-forever.properties` na raiz do jogo:

```properties
portal=auth.example.com
key=02596F0D0C061A8B30745988FD72C59E29EC367FB0F341F28E0F08D037BAFC69
```

`key` são 64 caracteres hex da chave **pública** (32 bytes). Valores inválidos caem no padrão.

**Diagnóstico:** `logs/classic-forever.log` na pasta do jogo e logcat com a tag `ClassicForever`. `Waiting for Wow-ARM64.exe...` = processo não encontrado (candidatos listados a cada 10 s); `Memory access denied` = SELinux bloqueou o acesso; `Waiting for realm connection` após entrar no mundo = atualize `KNOWN_KEYS` do script atual. Se o primeiro login no reino falhar, tente de novo **sem fechar o jogo**.

---

## Atualização, problemas e build

O atualizador integrado usa os CDNs públicos da Blizzard, sem PC. Se houver build nova, aparece o cartão de atualização (com **Launch Anyway (Outdated)**). Alternativa: `tools/sync_wow_to_device.sh`.

| Sintoma | Solução |
| :--- | :--- |
| *CAS system was unable to initialize* | Falta `.build.info` ou `_forever_/.flavor.info`. |
| Sem realms | Cliente desatualizado — toque em Update. |
| Teclado não aparece | Force a parada do Gboard e abra **Keyboard** de novo. |
| Volta ao launcher após iniciar | Verifique `_forever_/Errors/`. |
| Servidor privado não conecta | Veja `logs/classic-forever.log`: `Found game`, `Server public key written`, `READY`. |

Build: JDK 17, Android SDK + NDK 27.3.13750724, `tools/fetch_components.sh`, depois `./gradlew assembleModernRelease` (ou `assembleModernDebug`).

---

## Créditos e licença

Baseado em **[GameNative](https://github.com/utkarshdalal/GameNative)** (GPL-3.0), **[Pluvia](https://github.com/oxters168/Pluvia)**, **[Winlator](https://github.com/brunodev85/winlator)**, no bundle da comunidade **WoW Forever RP6** (Proton 11 ARM64EC, DXVK 2.4.1, Mesa Turnip), **[Wine](https://www.winehq.org/)**, **[box64](https://github.com/ptitSeb/box64)** e **[Blizzard Entertainment](https://worldofwarcraft.blizzard.com/en-us/forever)**. Detalhes em [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES). Licença GPL-3.0, veja [`LICENSE`](LICENSE).
