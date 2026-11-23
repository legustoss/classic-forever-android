# Contributing

## Reporting bugs

Use the issue templates and include:

- device model, SoC, Android version;
- app version and GPU/driver from the `Device compatibility` log line;
- steps to reproduce;
- `logs/app.log` and `logs/classic-forever.log` when relevant.

Never attach credentials, keystores or `.build.info` from a private install.

## Pull requests

- One logical change per commit, English commit message with the prefix from `AGENTS.md` (`perf(runtime):`, `refactor(patcher):`, `feat(config):`, `feat(diagnostics):`, `feat(ui):`, `feat(version):`, `docs:`, `ci:`).
- `./gradlew assembleModernDebug` must pass before opening a PR.
- Add or update unit tests for pure logic.
- Do not add dependencies without explaining APK size, GPL compatibility and startup impact.
- Do not modify `tools/`, keystores or release workflows without an explicit request.

## Scope

This is a single-purpose launcher for World of Warcraft: Forever on Snapdragon ARM64 devices. Please do not reintroduce the removed GameNative store backends, download systems or settings screens.

See [dev-setup.md](docs/dev-setup.md) and [release-checklist.md](docs/release-checklist.md).
