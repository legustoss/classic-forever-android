# Support plan

Current work items and their state are tracked in [ROADMAP.md](ROADMAP.md).

## Version lifecycle

| Phase | Trigger | Action |
| :--- | :--- | :--- |
| Active | Client build matches the server | Features and fixes |
| Maintenance | Blizzard released a new client, server not updated | Critical fixes only; updater offers "skip update" |
| Adapting | Server announces upgrade | Compatibility fixes and beta builds |
| Archived | Server shuts down or project is handed over | Read-only repository, final README |

## Dependency tracking

| Dependency | Source | Frequency |
| :--- | :--- | :--- |
| GameNative upstream | [utkarshdalal/GameNative](https://github.com/utkarshdalal/GameNative) | Monthly diff of `main` |
| DXVK | [doitsujin/dxvk](https://github.com/doitsujin/dxvk) | Quarterly evaluation |
| Turnip 740 / 8xx | [whitebelyash/mesa-unified](https://github.com/whitebelyash/mesa-unified), [arusiasotto/wow-forever-a840](https://github.com/arusiasotto/wow-forever-a840) | With upstream |
| Proton/Wine ARM64EC | [The412Banner/proton-wine](https://github.com/The412Banner/proton-wine) | With upstream |
| Classic Forever server | Discord announcements and community reports | Event-driven |

## Device roadmap

- Validate Adreno 750 (Snapdragon 8 Gen 3).
- Validate Adreno 735 / 825 (8s Gen 3 / 4).
- Consider Adreno 732 as a minimum playable tier after testing.

## Bus factor

- Keep at least two maintainers with merge rights.
- Keep [update-runtime.md](update-runtime.md) and [release-checklist.md](release-checklist.md) current.
- Rebase on GameNative upstream regularly to keep the fork diff small.
