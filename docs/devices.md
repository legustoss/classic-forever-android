# Device compatibility

GPU detection lives in `DeviceCompatibility.kt`. The client is a Windows ARM64 executable; Snapdragon ARM64 devices run it natively through Wine/Proton, with DXVK and Turnip for graphics.

| GPU | SoC examples | Status | Driver |
| :--- | :--- | :--- | :--- |
| Adreno 740 | Snapdragon 8 Gen 2 | Validated | `Turnip-WoW-scheduler-test` |
| Adreno 830 / 840 | Snapdragon 8 Elite | Validated | `Turnip-V32-RP6sched` |
| Adreno 750 | Snapdragon 8 Gen 3 | Pending validation | closest known driver, warning shown |
| Adreno 735 | Snapdragon 8s Gen 3 | Pending validation | closest known driver, warning shown |
| Adreno 825 | Snapdragon 8s Gen 4 (e.g. Poco F7) | Pending validation | `Turnip-V32-RP6sched`, warning shown |
| Adreno 850 | newer 8 Elite generation | Pending validation | `Turnip-V32-RP6sched`, warning shown |
| Unknown | any | Unsupported / warning | fallback driver, launch still allowed |

The launcher logs the result as:

```
Device compatibility: gpu=Adreno 825 driver=Turnip-V32-RP6sched status=PENDING_VALIDATION forced=false
```

## Manual override

Add to `classic-forever.properties` in the game root:

```
driver.force=8xx
```

Accepted values: `auto`, `740`, `8xx`, `turnip-740`, `turnip-8xx`, or the full driver id. An invalid value is ignored.

## Reporting a device

Use the **device report** issue template and attach `logs/app.log`. Include `adb shell getprop ro.board.platform` if available. Do not claim a device is validated without an in-world test.
