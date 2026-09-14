# Compatibility

This document is the maintained compatibility reference for Create: Precise Controls. Public project-page copy should stay concise and link here when version-specific behavior matters.

Current release: **0.1.1**.

## Base targets

| Minecraft | Loader | Java | Create | Status |
| --- | --- | --- | --- | --- |
| 1.20.1 | Forge | 17 | 6.0.8 | Supported |
| 1.21.1 | NeoForge | 21 | 6.0.11 | Supported |

Create is required on every supported target.

## Development runtime coverage

The normal dev clients intentionally load the optional integrations that Precise Controls supports so compatibility mixins are exercised during ordinary `runClient` testing:

| Target | FluidLogistics | Create: Factory Controller |
| --- | --- | --- |
| Forge 1.20.1 | 1.2.9 (Modrinth version `2Ls4IATF`) | 1.2.1-forge-1.20.1 |
| NeoForge 1.21.1 | 1.2.9 (Modrinth version `QcUiaW4c`) | 1.2.1-neoforge-1.21.1 |

These are dev/runtime-only dependencies. They are not declared as required dependencies of Create: Precise Controls and remain optional for users.

FluidLogistics publishes the same public version number (`1.2.9`) for both loader targets, so the dev builds pin the exact Modrinth version IDs to prevent cross-loader resolution. CI additionally keeps the older FluidLogistics 1.2.6 source contract on purpose because that release represents the legacy `FactoryPanelScreen` architecture that 1.2.9 no longer exercises.

## Create Factory Gauge

The base Factory Gauge integration is the primary feature and is supported on both base targets.

Supported precise-entry surfaces:

- Recipe ingredient count: Ctrl+right-click.
- Recipe output count: Ctrl+right-click.
- Stock-target value board: hold Use to open it, then hold Ctrl and release Use. With default controls, Use is RMB.

The target gesture follows Create's real lifecycle. Create opens `ValueSettingsScreen` only after Use has been held for several ticks, and the screen normally commits by calling `saveAndClose` when that same Use binding is released. Precise Controls intercepts only the Ctrl-modified `saveAndClose` path; unmodified release remains Create-owned. This also works when Use is rebound to a keyboard key rather than RMB.

The modifier on recipe slots is intentional. Create's current `FactoryPanelScreen.mouseClicked` disconnect path does not inspect the mouse button, so reserving Ctrl+RMB for precise entry leaves all unmodified Create clicks untouched.

## Create: Factory Controller

Factory Controller is optional and supported on both pinned 1.2.1 ports: Forge 1.20.1 and NeoForge 1.21.1. The compatibility bridge is shared across loaders; CI verifies its reflected `ConfigureRecipeScreen` fields/methods and `VirtualGaugeBehaviour` constants against the exact published JAR for each target, including Forge's production-obfuscated `m_6375_` click entry point.

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

CI checks Create 6.0.8 and 6.0.11 for the hold-Use -> open `ValueSettingsScreen` -> Use-release -> `saveAndClose` lifecycle, checks the optional bridges against pinned FluidLogistics 1.2.6/1.2.9 and Factory Controller source contracts, and inspects the exact published Factory Controller 1.2.1 Forge/NeoForge binaries for every private field/method/constant used by the reflection bridge. Shared tests cover the release-routing decision so ordinary release cannot accidentally become precise entry and Ctrl-modified Factory Gauge release cannot regress back to the unreachable second-click path.

For the 1.21.1 background regression, launch the normal NeoForge dev client, enter a world, open any precise numeric editor (for example Factory Gauge output with Ctrl+RMB), and verify the world remains sharp behind the editor while the editor's own dim/modal backdrop is still visible. Forge 1.20.1 should retain its existing unblurred behavior.

Before merging an addon-runtime change, manually exercise both normal dev clients with the pinned addons loaded: open FluidLogistics' resource gauge UI and Factory Controller's recipe/configure screen, use the Ctrl+RMB precise-entry paths, and confirm the logs contain no Precise Controls mixin/reflection failures. This complements the automated source/binary contracts with an actual screen-level runtime check.

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
