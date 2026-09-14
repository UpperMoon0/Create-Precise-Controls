# Create: Precise Controls

Stop fighting scroll wheels for exact Create values.

**Create: Precise Controls** is a client-side quality-of-life addon that lets you type exact numeric values into Create controls that normally rely on scrolling or repeated clicks. The original controls remain available; precise entry is added alongside them.

## Features

### Create Factory Gauge

- Right-click the target-amount value board and type an exact Items or Stacks target.
- Right-click recipe ingredients and outputs to enter exact counts.
- Enter stock targets beyond Create's normal 0-100 picker range, with overflow-safe limits.
- Keep using normal scrolling, Shift-scrolling, and Create's existing clicks whenever you prefer.

### Create: FluidLogistics compatibility

When FluidLogistics is installed, Precise Controls can add exact resource amount entry to supported resource recipe controls.

- Enter raw fluid amounts such as `1440 mB` directly.
- Uses FluidLogistics' own unit and request-cap information rather than hardcoded fluid limits.
- Supports the 1.2.6 Create-screen integration and the newer 1.2.9+ dedicated resource-gauge screen architecture.
- Supports exact target, restock-threshold, additional-stock, and promise-limit controls on the newer resource screen when those controls are present.

### Create: Factory Controller compatibility

On the supported NeoForge 1.21.1 integration, precise entry is available for:

- Regular item and fluid recipe ingredients.
- Non-crafting outputs.
- Craft batch size.
- Request multiplier.
- Request interval.
- Open-request limit.

Factory Controller's own native target/threshold text field is left alone.

## Client-side only

Create: Precise Controls adds no blocks, items, world data, commands, server state, or custom network protocol. It submits values through the same packet paths used by the original Create/addon interfaces.

**Multiplayer servers do not need this mod installed.** Install it on the client that wants the improved controls.

## Supported versions

| Minecraft | Loader | Create |
| --- | --- | --- |
| 1.20.1 | Forge | 6.0.8 |
| 1.21.1 | NeoForge | 6.0.11 |

Create is required. Create: Factory Controller and Create: FluidLogistics are optional integrations and are not required to use the base Factory Gauge features.

For exact addon compatibility details, see the repository's `docs/COMPATIBILITY.md`.

## Source and issues

Source code, documentation, changelogs, and issue tracking are maintained at the GitHub repository for Create: Precise Controls.

Licensed under the MIT License.
