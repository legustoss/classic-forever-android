# Performance results (device measurements)

Measured values only. Nothing here is estimated, and no variant is declared faster until the
same route has been run on every variant. Scenario definitions live in [`benchmarks.md`](benchmarks.md).

Snapshot: 2026-11-24.

## Device and build under test

| Field | Value |
| :--- | :--- |
| Device | Android test device (serial withheld) |
| Android | 16 |
| Board (`ro.board.platform`) | `sun` |
| Package | `app.wowforever` |
| Variant | `Classic-Forever-Proton-v16-TEST.apk` |
| Approval status | **Test build only — not approved as the recommended build** |

The v16 test APK carries the candidate Proton `proton-11.0-12.8-arm64ec` (versionCode 16). It is a
measurement artifact; see [`performance-candidates.md`](performance-candidates.md) for its provenance
and the rollback procedure. Do not treat it as the default.

## Scenario

Running around a city for roughly 60 s, captured 2026-11-24. Counters were reset at **17:03:50**
(device clock) immediately before the run.

## Capture protocol

```sh
# 1. Clear log buffers and zero the gfxinfo counters
adb logcat -c
adb shell dumpsys gfxinfo app.wowforever reset

# 2. Play the scenario (~60 s of running around the city)

# 3. Capture both artifacts
adb logcat -d -v time                          > logcat.txt
adb shell dumpsys gfxinfo app.wowforever framestats > framestats.txt
```

Artifacts from this run are saved outside the repo in:

```
%LOCALAPPDATA%\Temp\opencode\perf\v16-city\
```

The `reset` step is mandatory. Without it, `Total frames rendered` and the legacy jank counters
accumulate from app start (or from the previous reset), so the numbers no longer describe the run.
Always re-run the reset immediately before the scenario and record the reset timestamp.

## v16 test build — measured numbers

| Metric | Value |
| :--- | :--- |
| Total frames rendered | 3512 |
| Janky frames (modern) | 0 (0.00%) |
| Janky frames (legacy) | 2160 (61.50%) |
| Frame CPU time p50 | 21 ms |
| Frame CPU time p90 | 36 ms |
| Frame CPU time p95 | 46 ms |
| Frame CPU time p99 | 93 ms |
| Missed VSync | 0 |
| Slow UI thread | 0 |
| High input latency | 7024 |
| Frame deadline missed | 0 |
| GPU time p50 | 13 ms |
| GPU time p90 / p95 / p99 | 4950 ms (sentinel — see caveats) |

## Caveats

**The GPU p90/p95/p99 value of 4950 ms is not a frame time.** It is the sentinel value gfxinfo
writes when GPU frame timing is unavailable for the frame — the driver/hardware combination in this
run does not report per-frame GPU completion. It must not be read as a 4.95 s frame, and it must not
be averaged, charted or quoted as latency. Only the GPU p50 (13 ms) is a real reported value, and it
should be treated as incomplete on its own.

**Cumulative vs reset counters.** Most figures above describe only the window after the 17:03:50
reset; the `High input latency` counter is an explicit exception discussed below. A
`dumpsys gfxinfo ... framestats` dump taken without a preceding reset includes frames from
earlier in the session; comparing such a dump against this one is invalid. The logcat capture
(`adb logcat -d`) keeps the whole buffer, so it can contain lines from before the reset — filter by
timestamp when correlating log events with the run.

**Legacy vs modern jank.** The two jank figures use different definitions and both are reported by
`gfxinfo`. Modern jank (0 / 0.00%) is the current frame-deadline-based measure; legacy jank
(2160 / 61.50%) uses the older threshold-based definition, which flags frames the modern measure
considers on time. The legacy number is expected to be much higher and does not by itself mean the
run stuttered. Use the modern figure plus the percentile table for comparisons, and keep both
columns when reporting.

**"High input latency" 7024 exceeds the 3512 rendered frames.** That is not a frame count in the
same window, so it is reported here verbatim but not interpreted. It likely comes from a counter
that the reset does not clear, or from different accounting; treat it as unresolved until the
capture is repeated and cross-checked.

**Average frame rate is derived, not measured.** 3512 frames over a capture window of
approximately 60 s works out to roughly 58 fps. The window was not precisely timed, so this is a
rough sanity check, not a result.

**Not comparable yet.** The baseline APK and the stripped-Proton APK have not been run on this
route. No conclusion can be drawn about which Proton build is faster from this single measurement.

## Comparison

| Variant | Scenario | Result |
| :--- | :--- | :--- |
| `Classic-Forever-Proton-v16-TEST.apk` | city run, ~60 s, 2026-11-24 | 3512 frames, modern jank 0.00%, CPU p50 21 ms / p99 93 ms (see table above) |
| `Classic-Forever-baseline.apk` | same route | **not measured yet** |
| `Classic-Forever-Proton-stripped-TEST.apk` | same route | **not measured yet** |

Until the baseline row is filled in, this page records a measurement, not a comparison.
