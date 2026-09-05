# Changelog

## 0.1.0 - 2026-09-06

### Added

- Added exact keyboard entry for Create Factory Gauge recipe ingredient and output counts.
- Added exact Factory Gauge stock-target entry from Create's value-settings board, including values beyond the original 0-100 picker range.
- Added optional Create: Factory Controller exact entry for regular recipe ingredients and outputs, including mB fluid amounts.
- Added Create: FluidLogistics compatibility for exact resource recipe amounts, deriving units and request caps from FluidLogistics itself.
- Added compatibility with FluidLogistics 1.2.6's FactoryPanelScreen mixin and 1.2.9+'s dedicated ResourceFactoryGaugeScreen.
- Added exact Factory Controller craft-batch, request-multiplier, request-interval, and open-request-limit entry.
- Added Forge 1.20.1 / Create 6.0.8 and NeoForge 1.21.1 / Create 6.0.11 targets from a shared multi-version codebase.
- Added loader-specific bridges that submit values through Create's existing networking instead of introducing a custom protocol.
- Added shared numeric parsing tests and English localization.

### Compatibility

- Create: Factory Controller support is optional and isolated behind a client `@Pseudo` mixin.
- Create: FluidLogistics support is optional; its public package-resource API is discovered at runtime and its newer resource screen is isolated behind a client `@Pseudo` mixin.
- The mod is client-only; multiplayer servers do not need Create: Precise Controls installed.
