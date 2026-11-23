# FAQ

## "CAS system was unable to initialize"

`.build.info` is missing or invalid in the game root. Copy the hidden `.build.info` from the Blizzard install root, next to `Data/`.

## No realms are listed

The installed client build does not match the live realm build. Check **Update** on the launcher. If the server has not adapted to the newest build, add `server.min.build` / `server.max.build` to `classic-forever.properties` and use **Skip this update**.

## "Client outdated, please update" appears but I cannot update

Your build is below `server.min.build`. Either update the game or wait for the server to support your build. The launcher blocks launch for too-old builds.

## "Client newer than the server maximum"

You may launch anyway, but the server may not support the build yet. This is a warning, not a block.

## Keyboard does not appear

Force-stop Gboard, then open **Keyboard** from the in-game menu again.

## The game returns to the launcher right after "Launching Game…"

Check `_forever_/Errors/` on the device and `logs/app.log`.

## Private server does not connect

Open `logs/classic-forever.log` and look for:

- `Found game` — process detected;
- `Server public key written` — patch applied;
- `READY` — patcher finished.

If the first realm login fails, retry **without closing the game**. If the log says `Waiting for realm connection`, update `KNOWN_KEYS` from the current `classic-forever-linux.py`.

## A front-end shows the wrong icon

The front-end cached old artwork. Refresh the icon in the front-end or reinstall the app.

## Where are the logs?

In the game root, next to `Data/`: `logs/classic-forever.log` and `logs/app.log`. Both rotate to `.old` at 2 MB.

## Is this affiliated with Blizzard?

No. It is an unofficial community project. It contains no Blizzard game files.
