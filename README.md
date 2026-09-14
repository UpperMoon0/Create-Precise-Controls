# Create: Precise Controls

Client-side quality-of-life controls for Create. Precise Controls adds keyboard entry to numeric controls that otherwise require scrolling or repeated clicks, while leaving the original interaction available.

## Supported targets

| Minecraft | Loader | Create |
| --- | --- | --- |
| 1.20.1 | Forge | 6.0.8 |
| 1.21.1 | NeoForge | 6.0.11 |

Create: Factory Controller and Create: FluidLogistics integrations are optional. Factory Controller currently follows its NeoForge 1.21.1 `1.2.1-alpha.3` recipe-screen layout. FluidLogistics compatibility covers both its 1.2.6 Create-screen mixin architecture and the newer 1.2.9 dedicated resource-gauge screen.

See [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) for the maintained compatibility matrix and version-specific notes.

## Controls

### Create Factory Gauge

- **Hold Use to open the target-amount board, then hold Ctrl and release Use** to type an exact Items or Stacks target. With default controls, Use is RMB. Large values are supported beyond Create's 0-100 picker range, with a safe cap that prevents the resulting demand from overflowing Create's integer count model.
- **Ctrl+right-click a recipe ingredient** to type its exact count.
- **Ctrl+right-click the recipe output** to type its exact count.
- Ordinary Use release still commits Create's native target selection. Ordinary left/right clicks, normal scrolling, and Shift-scrolling keep their Create behavior.

### Create: FluidLogistics

When FluidLogistics is installed:

- **Ctrl+right-click a resource recipe ingredient/output** to type the raw amount directly. Fluid recipes therefore accept values such as `1440 mB` without mouse-wheel stepping.
- Precise Controls asks FluidLogistics' `PackageResources` / `PackageResourceDisplay` API for the resource unit and `maxRequestPerBatch`. Fluid caps are not hardcoded in this mod.
- FluidLogistics 1.2.6 modifies Create's normal `FactoryPanelScreen`; that path supports exact recipe amounts plus its injected restock-threshold, additional-stock, and promise-limit controls. Those settings are committed through the screen's existing `sendIt` path so FluidLogistics emits its own packet.
- FluidLogistics 1.2.9+ uses its own `ResourceFactoryGaugeScreen`; the optional compatibility mixin handles that screen as well, including exact target, restock-threshold, additional-stock, and promise-limit controls when present.
- The 1.2.6 target-amount value board is intentionally left to FluidLogistics. In that release it encodes a unit row/value pair before converting to the underlying resource amount, so treating a typed mB amount as a raw Create `ValueSettingsPacket` value would be incorrect.

### Create: Factory Controller

When Factory Controller is installed on NeoForge 1.21.1, use **Ctrl+right-click** for precise entry on supported regions:

- A REGULAR ingredient for an exact item or fluid amount.
- A non-crafting output for an exact item or fluid amount.
- The crafting output to type the craft batch directly.
- The request multiplier within the controller's current structural cap.
- The request interval arrow from 1 to 60 seconds.
- The open-request limit from 0 to 99.

Unmodified RMB keeps Factory Controller's own reset and scope-toggle actions. Fluid limits are read from Factory Controller's `VirtualGaugeBehaviour` at runtime instead of duplicated here; on the pinned compatibility source they are 90,000 mB per regular fluid ingredient and 10,000 mB for fluid output. Factory Controller's target/threshold field already supports native click-to-type input, so Precise Controls deliberately does not replace it.

## Client-only

The mod is client-side only. It adds no blocks, items, data, commands, server state, or custom network protocol. Exact values are committed through the same Create or Factory Controller packet path their original UI uses. A multiplayer server does **not** need Create: Precise Controls installed.

The Forge metadata ignores server-version matching, loader dependencies are client-scoped, and all mixins are declared as client mixins. Installing the jar on a dedicated server is unnecessary.

## Implementation

The base integration uses small Mixins against Create's existing GUI classes rather than replacing screens wholesale:

- `FactoryPanelScreenMixin` adds exact entry to Factory Gauge recipe counts and legacy FluidLogistics-injected controls.
- `ValueSettingsScreenMixin` intercepts Create's existing `saveAndClose` Use-release lifecycle only for Ctrl-modified Factory Gauge target entry; it does not wait for a second RMB click.
- `AbstractSimiScreenAccessor` reads layout fields from the Catnip superclass where those fields are actually declared.
- `ScrollInputAccessor` reads the range already configured by Create/addon widgets rather than duplicating it.
- Loader-specific `ValueSettingsSender` classes bridge Create 1.20.1 and 1.21.1 networking while keeping the UI code shared.

Factory Controller has its own recipe screen instead of using Create's `FactoryPanelScreen`. Its integration is therefore isolated in an optional `@Pseudo` compatibility mixin. FluidLogistics is handled through its public package-resource display API plus an optional `@Pseudo` mixin for its newer dedicated resource-gauge screen. Neither addon is linked as a required dependency.

CI pins Create 6.0.8/6.0.11 plus the supported FluidLogistics and Factory Controller source revisions. It verifies Create's hold-Use/open/release/save lifecycle as well as the private/source contracts used by optional reflection and mixin compatibility. Shared regression tests verify that Ctrl-modified Factory Gauge release diverts to the exact editor while ordinary release remains Create-owned.

For the design boundaries and extension rules, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Project layout

```text
common/                 Shared loader-independent numeric parsing/tests
shared/                 Shared Minecraft/Create client UI and mixins
forge-1.20.1/           Forge 1.20.1 loader + Create networking bridge
neoforge-1.21.1/        NeoForge 1.21.1 loader + Create networking bridge
```

## Development

Build every target with:

```shell
./gradlew buildAll
```

Run shared and loader tests with:

```shell
./gradlew testAll
```

The standard Forge 1.20.1 and NeoForge 1.21.1 `runClient` configurations also load the matching FluidLogistics 1.2.9 and Create: Factory Controller 1.2.1 builds as dev/runtime-only dependencies. This keeps optional compatibility code active during normal client testing without making either addon a user dependency. Exact addon pins live in each target's `gradle.properties`.

## Documentation and releases

- [Compatibility matrix](docs/COMPATIBILITY.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Release process](docs/RELEASING.md)
- [CurseForge project-page copy](CURSEFORGE.md)
- [Full changelog](CHANGELOG.md)
- Release-specific notes live under [`changelog/`](changelog/).

The repository currently uses `0.1.0-SNAPSHOT` for development. A release is published only after `mod_version` is changed to a non-SNAPSHOT version with a matching `changelog/<version>.md` file.

## License

Create: Precise Controls is available under the [MIT License](LICENSE).
