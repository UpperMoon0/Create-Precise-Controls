# Create: Precise Controls

**Type exact amounts into Create's Factory Gauges.**

Set the stock target or recipe amount you want with your keyboard. Precise Controls adds exact number entry to Factory Gauges and supported Factory Controller and FluidLogistics settings.

**Client-side only. Install it on your client; multiplayer servers do not need it.**

## Features

- **Exact stock targets:** type how many items or stacks your Factory Gauge should keep in stock.
- **Exact recipe amounts:** type ingredient and output counts directly.
- **Larger stock targets:** go beyond Create's normal 0–100 picker range, within safe limits.
- **FluidLogistics support:** enter exact item and fluid recipe amounts, including values such as `1440 mB`, plus supported stock and restock settings.
- **Factory Controller support:** type recipe amounts, craft batch sizes, request multipliers, request intervals, and open-request limits.

Normal clicks and scrolling remain available.

## How to use it

**Factory Gauge stock target:** hold Use to open the target-amount board, then hold **Ctrl** and release **Use**. Type your value and confirm. With default keybindings, Use is right-click.

**Factory Gauge recipe amounts:** **Ctrl+right-click an ingredient or output** to type its exact count.

**Supported addon fields:** **Ctrl+right-click** to open exact entry. Values follow the addon's own limits.

## Requirements

| Minecraft | Loader | Minimum Create version |
| --- | --- | --- |
| 1.20.1 | Forge | 6.0.8 |
| 1.21.1 | NeoForge | 6.0.11 |

The listed versions are minimums and tested build targets, not exact pins. Newer Create updates are accepted by loader metadata but must be tested for API and UI compatibility.

**Create is required.** Factory Controller and FluidLogistics are optional.

Factory Controller **1.2.1** is supported on both targets. Its stock target/threshold field already supports typing; Precise Controls adds exact entry to the other supported settings.

FluidLogistics support covers **1.2.6** and **1.2.9+** screen layouts. Exact stock target entry depends on the screen: the 1.2.6 target-amount board keeps FluidLogistics' original interaction.

See the [compatibility guide](https://github.com/UpperMoon0/Create-Precise-Controls/blob/main/docs/COMPATIBILITY.md) for detailed addon support.

## Source and support

- [Source code](https://github.com/UpperMoon0/Create-Precise-Controls)
- [Report a bug](https://github.com/UpperMoon0/Create-Precise-Controls/issues)
- [Changelog](https://github.com/UpperMoon0/Create-Precise-Controls/blob/main/CHANGELOG.md)

Licensed under the [MIT License](https://github.com/UpperMoon0/Create-Precise-Controls/blob/main/LICENSE).
