# Architecture

Create: Precise Controls is intentionally small: it augments existing Create/addon client screens instead of replacing them or creating a parallel networking protocol.

## Module layout

| Module | Responsibility |
| --- | --- |
| `common/` | Loader-independent numeric parsing and unit-testable logic. |
| `shared/` | Shared Minecraft/Create UI code, mixins, optional compatibility code, assets, and localization. |
| `forge-1.20.1/` | Forge 1.20.1 bootstrap, metadata, dependencies, and Create networking bridge. |
| `neoforge-1.21.1/` | NeoForge 1.21.1 bootstrap, metadata, dependencies, and Create networking bridge. |

## Input flow

The normal precise-entry flow is:

1. A supported Create/addon screen receives the precise-entry interaction.
2. `ExactAmountScreen` collects and validates keyboard input.
3. Shared mixin code translates that number into the value expected by the owning screen.
4. A loader-specific `ValueSettingsSender` or the owning addon's existing path submits the value.
5. The server sees the same protocol/state transition it would see from the original UI.

This is why the mod can remain client-only.

## Base Create integration

The main shared mixins are deliberately narrow:

- `FactoryPanelScreenMixin` augments Factory Gauge recipe ingredient/output controls.
- `ValueSettingsScreenMixin` augments the value-settings screen only when it belongs to a Factory Gauge.
- `AbstractSimiScreenAccessor` exposes layout state from the Catnip superclass where that state is declared.

The design rule is to enhance an existing screen in place and leave unrelated Create screens unchanged.

## Optional addon integration

Optional integrations live under compatibility-specific packages.

### Factory Controller

`ConfigureRecipeScreenMixin` is an optional `@Pseudo` mixin. Factory Controller must remain optional at class-load time; base Create functionality must not depend on its classes being installed.

### FluidLogistics

`FluidLogisticsCompat` handles package-resource semantics and obtains resource-unit/request-cap information from FluidLogistics itself. `ResourceFactoryGaugeScreenMixin` handles the newer dedicated resource-gauge screen as an optional compatibility mixin.

Do not duplicate addon limits in generic parsing logic when the addon exposes the authoritative value.

## Version boundaries

Shared source is appropriate only while the targeted APIs have compatible semantics. Loader/network differences belong behind loader-specific bridge classes rather than version conditionals scattered through the UI mixins.

When a dependency changes a screen layout or packet contract, verify the real target classes before updating mixin targets. Do not infer compatibility from a neighboring Minecraft version.

## Testing expectations

Before merging a behavior change:

- `./gradlew testAll` must pass.
- `./gradlew buildAll` must produce both supported loader artifacts.
- Numeric parsing edge cases should remain in `common` tests where possible.
- Optional compatibility changes should be checked against the exact addon version/layout they target.
- A client-only change must not introduce server-only class loading or a custom protocol accidentally.

CI builds the Forge 1.20.1 and NeoForge 1.21.1 targets independently with their required Java versions.
