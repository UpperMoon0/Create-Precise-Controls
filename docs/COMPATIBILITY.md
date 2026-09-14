# Compatibility

This document is the maintained compatibility reference for Create: Precise Controls. Public project-page copy should stay concise and link here when version-specific behavior matters.

## Base targets

| Minecraft | Loader | Java | Create | Status |
| --- | --- | --- | --- | --- |
| 1.20.1 | Forge | 17 | 6.0.8 | Supported |
| 1.21.1 | NeoForge | 21 | 6.0.11 | Supported |

Create is required on every supported target.

## Create Factory Gauge

The base Factory Gauge integration is the primary feature and is supported on both base targets.

Supported precise-entry surfaces:

- Recipe ingredient count.
- Recipe output count.
- Stock-target value board.

The normal Create interactions remain available.

## Create: Factory Controller

Factory Controller is optional. The currently documented integration follows the NeoForge 1.21.1 `1.2.1-alpha.3` recipe-screen layout.

Supported surfaces on that integration:

- REGULAR item/fluid ingredient amount.
- Non-crafting item/fluid output amount.
- Craft batch.
- Request multiplier.
- Request interval from 1 to 60 seconds.
- Open-request limit from 0 to 99.

Factory Controller's target/threshold field already provides native click-to-type behavior and is intentionally not replaced.

The compatibility mixin is optional (`@Pseudo`) so Factory Controller is not a required dependency.

## Create: FluidLogistics

FluidLogistics is optional. Precise Controls recognizes two relevant UI architectures:

- **1.2.6:** FluidLogistics modifies Create's normal `FactoryPanelScreen`. Precise Controls enhances the compatible recipe controls in that screen.
- **1.2.9+:** FluidLogistics provides a dedicated `ResourceFactoryGaugeScreen`. An optional compatibility mixin handles that screen and supported exact target/restock/additional-stock/promise-limit controls.

Resource units and `maxRequestPerBatch` are obtained from FluidLogistics' package-resource API rather than duplicated as hardcoded limits.

The 1.2.6 target-amount value board is deliberately not treated as a raw Create value because that release encodes a unit row/value pair before converting it to the underlying resource amount.

Compatibility code only activates when the corresponding addon classes are present. Actual installable combinations are also constrained by which Minecraft/loader versions each addon publishes.

## Client/server requirements

Create: Precise Controls is client-side only:

- No blocks or items.
- No world or persistent server state.
- No commands.
- No custom network protocol.
- Existing Create/addon packet paths are used to submit the selected value.

A multiplayer server does not need Create: Precise Controls installed.

## Updating compatibility

When changing any dependency version:

1. Update the relevant target `gradle.properties`.
2. Verify the affected screen/method layout against the actual dependency source or decompiled classes.
3. Run `./gradlew testAll` and `./gradlew buildAll`.
4. Update this file, `README.md`, and `CURSEFORGE.md` if user-facing support changed.
5. Record the change in `CHANGELOG.md` and the release-specific `changelog/<version>.md` file.
