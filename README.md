# Create: Precise Controls

Client-side quality-of-life controls for Create. The first feature adds exact keyboard entry to Factory Gauge recipe amounts that otherwise require mouse-wheel adjustment.

## Supported targets

- Forge 1.20.1 with Create 6.0.8
- NeoForge 1.21.1 with Create 6.0.11
- Optional Create: Factory Controller compatibility on NeoForge 1.21.1, currently matched to the 1.2.1-alpha.3 source layout

## Current interaction

Right-click a configurable Factory Gauge recipe ingredient or output slot to open an exact numeric entry dialog. Existing scrolling and left-click behaviour remain unchanged.

For Create: Factory Controller, v0.1 handles REGULAR-mode ingredient totals and non-crafting output amounts, including fluid values in mB. It deliberately uses the limits present in Factory Controller's source: 90,000 mB per fluid ingredient and 64,000 mB for fluid output.

## Design

The vanilla Create integration is a small client mixin against `FactoryPanelScreen`. Factory Controller is an optional compatibility mixin using `@Pseudo`, so the addon does not hard-depend on Factory Controller. The compatibility code is isolated because Factory Controller's recipe screen is its own reimplementation rather than Create's `FactoryPanelScreen`.

No new network protocol is introduced. The dialog edits the same client-side values that Create or Factory Controller already serializes when its parent screen is saved.
