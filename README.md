# Create: Precise Controls

**Type exact amounts into Create's Factory Gauges.**

Set stock targets and recipe amounts with your keyboard. Precise Controls adds exact entry to Factory Gauges, with optional support for Create: Factory Controller and Create: FluidLogistics.

**Client-side only. Multiplayer servers do not need this mod installed.**

## What you can set

- **Stock targets:** enter the exact number of items or stacks you want a Factory Gauge to keep in stock.
- **Recipe amounts:** type ingredient and output counts directly.
- **Larger targets:** enter stock targets beyond Create's normal 0–100 picker range, within safe limits.
- **Fluid amounts with FluidLogistics:** type amounts such as `1440 mB` directly into supported recipe fields.
- **Factory Controller settings:** type recipe amounts, craft batch sizes, request multipliers, request intervals, and open-request limits.

Normal clicks and scrolling remain available.

## How to use it

### Factory Gauge stock targets

1. Hold **Use** on the target setting to open Create's target-amount board.
2. Hold **Ctrl**, then release **Use** to open the exact amount editor.
3. Type the number you want and confirm.

With default keybindings, **Use** is right-click. Releasing Use without Ctrl confirms Create's normal selection.

### Factory Gauge recipes

**Ctrl+right-click an ingredient or output** to type its exact count.

### Optional addon support

| Addon | Exact entry |
| --- | --- |
| Create: FluidLogistics | Item/fluid recipe amounts and supported stock targets, restock thresholds, additional-stock amounts, and promise limits. |
| Create: Factory Controller 1.2.1 | Regular ingredient amounts, non-crafting output amounts, craft batch size, request multiplier, request interval (1–60 seconds), and open-request limit (0–99). |

Use **Ctrl+right-click** on supported addon fields. Values follow the addon's own limits.

FluidLogistics support covers its 1.2.6 and 1.2.9+ screen layouts. Its 1.2.6 target-amount board keeps the addon's original interaction; exact target entry is available on supported newer screens.

Factory Controller's stock target/threshold field already accepts typed values. Its normal right-click reset and scope-toggle actions remain available.

See the [compatibility guide](docs/COMPATIBILITY.md) for version-specific details.

## Installation

Install the matching Precise Controls jar on your client alongside Create.

| Minecraft | Loader | Supported Create version |
| --- | --- | --- |
| 1.20.1 | Forge | 6.0.8 |
| 1.21.1 | NeoForge | 6.0.11 |

Factory Controller and FluidLogistics are optional. The base Factory Gauge features work with Create alone.

## Development

The project shares its client UI and compatibility code across Forge 1.20.1 and NeoForge 1.21.1. Exact values are submitted through Create's or the addon's existing packet paths.

Build all targets:

```shell
./gradlew buildAll
```

Run shared and loader tests:

```shell
./gradlew testAll
```

The development clients include the matching optional addons for compatibility testing. See the [architecture guide](docs/ARCHITECTURE.md), [compatibility guide](docs/COMPATIBILITY.md), and [release process](docs/RELEASING.md) for implementation and verification details.

## Links

- [Report a bug](https://github.com/UpperMoon0/Create-Precise-Controls/issues)
- [Changelog](CHANGELOG.md)
- [CurseForge project-page copy](CURSEFORGE.md)

Licensed under the [MIT License](LICENSE).
