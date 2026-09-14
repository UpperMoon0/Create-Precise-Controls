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

- Recipe ingredient count: Ctrl+right-click.
- Recipe output count: Ctrl+right-click.
- Stock-target value board: hold Use to open it, then hold Ctrl and release Use. With default controls, Use is RMB.

The target gesture follows Create's real lifecycle. Create opens `ValueSettingsScreen` only after Use has been held for several ticks, and the screen normally commits by calling `saveAndClose` when that same Use binding is released. Precise Controls intercepts only the Ctrl-modified `saveAndClose` path; unmodified release remains Create-owned. This also works when Use is rebound to a keyboard key rather than RMB.

The modifier on recipe slots is intentional. Create's current `FactoryPanelScreen.mouseClicked` disconnect path does not inspect the mouse button, so reserving Ctrl+RMB for precise entry leaves all unmodified Create clicks untouched.

## Create: Factory Controller

Factory Controller is optional. The currently documented integration follows the NeoForge 1.21.1 `1.2.1-alpha.3` recipe-screen layout.

Supported surfaces use Ctrl+right-click:

- REGULAR item/fluid ingredient amount.
- Non-crafting item/fluid output amount.
- Craft batch.
- Request multiplier.
- Request interval from 1 to 60 seconds.
- Open-request limit from 0 to 99.

Unmodified RMB remains owned by Factory Controller. In particular, multiplier reset, request-interval reset, and open-request scope toggling are not intercepted.

Factory Controller's target/threshold field already provides native click-to-type behavior and is intentionally not replaced. The compatibility bridge reads `FLUID_INGREDIENT_CAP_MB`, `FLUID_OUTPUT_CAP_MB`, and the dynamic craft/item caps from Factory Controller rather than copying those limits. At the pinned compatibility revision, the fluid caps are 90,000 mB for a regular ingredient and 10,000 mB for output.

The compatibility mixin is optional (`@Pseudo`) so Factory Controller is not a required dependency.

## Create: FluidLogistics

FluidLogistics is optional. Precise Controls recognizes two relevant UI architectures:

- **1.2.6:** FluidLogistics modifies Create's normal `FactoryPanelScreen`. Precise Controls supports Ctrl+RMB exact recipe amounts and the injected restock-threshold, additional-stock, and promise-limit ScrollInputs. The latter commit through Create's existing `sendIt` method so FluidLogistics' own send hook emits the authoritative setting packet.
- **1.2.9+:** FluidLogistics provides a dedicated `ResourceFactoryGaugeScreen`. An optional compatibility mixin handles Ctrl+RMB exact recipe amounts and supported exact target/restock/additional-stock/promise-limit controls.

Resource units and `maxRequestPerBatch` are obtained from FluidLogistics' package-resource API rather than duplicated as hardcoded limits. ScrollInput ranges are read from the widgets themselves.

The 1.2.6 target-amount value board is deliberately not treated as a raw Create value because that release encodes a unit row/value pair before converting it to the underlying resource amount.

Compatibility code only activates when the corresponding addon classes are present. Actual installable combinations are also constrained by which Minecraft/loader versions each addon publishes.

## Compatibility verification

CI checks Create 6.0.8 and 6.0.11 for the hold-Use -> open `ValueSettingsScreen` -> Use-release -> `saveAndClose` lifecycle, and checks the optional bridges against pinned FluidLogistics 1.2.6, FluidLogistics 1.2.9, and Create: Factory Controller sources. Shared tests cover the release-routing decision so ordinary release cannot accidentally become precise entry and Ctrl-modified Factory Gauge release cannot regress back to the unreachable second-click path.

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
3. Update the pinned source-contract revisions when a Create/addon lifecycle or layout intentionally changes.
4. Run `./gradlew testAll` and `./gradlew buildAll`.
5. Update this file, `README.md`, and `CURSEFORGE.md` if user-facing support changed.
6. Record the change in `CHANGELOG.md` and the release-specific `changelog/<version>.md` file.
