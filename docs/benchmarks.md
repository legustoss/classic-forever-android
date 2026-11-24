# Benchmark scenarios

No FPS numbers are stored here without a real measurement. Run these scenarios on the device and record the result in an issue or release notes. Actual device numbers are kept in [`performance-results.md`](performance-results.md).

## Baseline

- Device, SoC, Android version, GPU (from `DeviceCompatibility` log).
- App version and driver id.
- Resolution and `RenderScale` from `WTF/Config.wtf`.

## Scenarios

1. **Login → realm selection** — measure startup time and shader-compilation hitches.
2. **Capital city, stationary** — 60 s, record FPS/frame time.
3. **Capital city, camera rotation** — 60 s, record FPS, stutter, GPU temperature if available.
4. **First visit to a new zone** — measure streaming hitches and shader compilation.
5. **Combat / many players** — 60 s, record worst frame time.

## Tools

- In-game performance HUD (FPS/CPU/RAM).
- `adb shell dumpsys gfxinfo app.wowforever`
- `adb shell dumpsys gfxinfo app.wowforever reset` immediately before each scenario, otherwise the counters include everything since app start.
- Perfetto system trace (CPU/GPU scheduling, long tasks).
- `logs/app.log` and `logs/classic-forever.log` for driver and patcher events.

## Recording format

```
Device: AYN Odin 2 / Android 14 / Adreno 740
App: 0.1.1 / driver Turnip-WoW-scheduler-test
Scenario: capital city, 1080p, RenderScale 0.5
Result: <measured FPS>, <observed stutter>, <temperature>
```

Do not estimate numbers. If a scenario was not run, write "not measured".
