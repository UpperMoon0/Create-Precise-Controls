# Create: Precise Controls

Client-side quality-of-life controls for Create. Precise Controls adds keyboard entry to numeric controls that otherwise require scrolling or repeated clicks, while leaving the original interaction available.

## Supported targets

| Minecraft | Loader | Create |
| --- | --- | --- |
| 1.20.1 | Forge | 6.0.8 |
| 1.21.1 | NeoForge | 6.0.11 |

Create: Factory Controller and Create: FluidLogistics integrations are optional. Factory Controller currently follows its NeoForge 1.21.1 `1.2.1-alpha.3` recipe-screen layout. FluidLogistics compatibility covers both its 1.2.6 Create-screen mixin architecture and the newer 1.2.9 dedicated resource-gauge screen.

## Controls

### Create Factory Gauge

- **Middle-click the target-amount value board** to type an exact Items or Stacks target. Large values are supported beyond Create's 0-100 picker range, with a safe cap that prevents the resulting demand from overflowing Create's integer count model.
- **Right-click or middle-click a recipe ingredient** to type its exact count.
- **Right-click or middle-click the recipe output** to type its exact count.
- Normal scrolling, Shift-scrolling, and Create's existing clicks are unchanged.

### Create: FluidLogistics

When FluidLogistics is installed:

- **Right-click or middle-click a resource recipe ingredient/output** to type the raw amount directly. Fluid recipes therefore accept values such as `1440 mB` without mouse-wheel stepping.
- Precise Controls asks FluidLogistics' `PackageResources` / `PackageResourceDisplay` API for the resource unit and `maxRequestPerBatch`. Fluid caps are not hardcoded in this mod.
- FluidLogistics 1.2.6 modifies Create's normal `FactoryPanelScreen`; that path is enhanced in place.
- FluidLogistics 1.2.9+ uses its own `ResourceFactoryGaugeScreen`; the optional compatibility mixin handles that screen as well, including exact target, restock-threshold, additional-stock, and promise-limit controls when present.
- The 1.2.6 target-amount value board is intentionally left to FluidLogistics. In that release it encodes a unit row/value pair before converting to the underlying resource amount, so treating a typed mB amount as a raw Create `ValueSettingsPacket` value would be incorrect.
### Create: Factory Controller

When Factory Controller is installed on NeoForge 1.21.1:

- **Right-click or middle-click a REGULAR ingredient** for an exact item or fluid amount.
- **Right-click or middle-click a non-crafting output** for an exact item or fluid amount.
- **Right-click or middle-click the crafting output** to type the craft batch directly.
- **Middle-click the request multiplier** to type an exact multiplier within the controller's current structural cap.
- **Middle-click the request interval arrow** to type an exact interval from 1 to 60 seconds.
- **Middle-click the open-request limit** to type a value from 0 to 99.

Fluid values use the units and limits defined by Factory Controller itself: up to 90,000 mB per regular fluid ingredient and 64,000 mB for fluid output. Factory Controller's target/threshold field already supports native click-to-type input, so Precise Controls deliberately does not replace it.

## Client-only

The mod is client-side only. It adds no blocks, items, data, commands, server state, or custom network protocol. Exact values are committed through the same Create or Factory Controller packet path their original UI uses. A multiplayer server does **not** need Create: Precise Controls installed.

The Forge metadata ignores server-version matching, loader dependencies are client-scoped, and all mixins are declared as client mixins. Installing the jar on a dedicated server is unnecessary.

## Implementation

The base integration uses small Mixins against Create's existing GUI classes rather than replacing screens wholesale:

- `FactoryPanelScreenMixin` adds exact entry to Factory Gauge recipe counts.
- `ValueSettingsScreenMixin` adds exact target entry only when the value-settings screen belongs to a Factory Gauge.
- `AbstractSimiScreenAccessor` reads layout fields from the Catnip superclass where those fields are actually declared.
- Loader-specific `ValueSettingsSender` classes bridge Create 1.20.1 and 1.21.1 networking while keeping the UI code shared.

Factory Controller has its own recipe screen instead of using Create's `FactoryPanelScreen`. Its integration is therefore isolated in an optional `@Pseudo` compatibility mixin. FluidLogistics is handled through its public package-resource display API plus an optional `@Pseudo` mixin for its newer dedicated resource-gauge screen. Neither addon is linked as a required dependency.

## Project layout

```text
common/                 Shared loader-independent numeric parsing/tests
shared/                 Shared Minecraft/Create client UI and mixins
forge-1.20.1/           Forge 1.20.1 loader + Create networking bridge
neoforge-1.21.1/        NeoForge 1.21.1 loader + Create networking bridge
```

Build every target with:

```shell
./gradlew buildAll
```

Run shared and loader tests with:

```shell
./gradlew testAll
```
