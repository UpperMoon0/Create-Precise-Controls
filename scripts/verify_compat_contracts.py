#!/usr/bin/env python3
"""Verify the source contracts used by optional compatibility mixins."""

from __future__ import annotations

import argparse
from pathlib import Path


def require(path: Path, markers: list[str]) -> None:
    if not path.is_file():
        raise SystemExit(f"missing compatibility source: {path}")
    text = path.read_text(encoding="utf-8")
    missing = [marker for marker in markers if marker not in text]
    if missing:
        details = "\n".join(f"  - {marker}" for marker in missing)
        raise SystemExit(f"compatibility contract changed in {path}:\n{details}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--self", dest="self_root", type=Path, default=Path("."))
    parser.add_argument("--fluid-126", type=Path, required=True)
    parser.add_argument("--fluid-129", type=Path, required=True)
    parser.add_argument("--factory-controller", type=Path, required=True)
    args = parser.parse_args()

    require(
        args.fluid_126 / "src/main/java/com/yision/fluidlogistics/mixin/client/FactoryPanelScreenMixin.java",
        [
            "fluidlogistics$restockThresholdInput",
            "fluidlogistics$additionalStockInput",
            "fluidlogistics$promiseLimitInput",
            'method = "sendIt"',
            "FactoryPanelSetResourceRestockSettingPacket",
        ],
    )
    require(
        args.fluid_126 / "src/main/java/com/yision/fluidlogistics/api/packager/PackageResourceDisplay.java",
        ["String baseUnit();", "factoryPanelRestockPolicy", "maxRequestPerBatch"],
    )

    require(
        args.fluid_129
        / "src/main/java/com/yision/fluidlogistics/content/logistics/factoryGauge/client/ResourceFactoryGaugeScreen.java",
        [
            "targetAmountInput",
            "restockThresholdInput",
            "additionalStockInput",
            "promiseLimitInput",
            "private final ResourceFactoryGaugeScreenState state",
        ],
    )
    require(
        args.fluid_129
        / "src/main/java/com/yision/fluidlogistics/content/logistics/factoryGauge/client/ResourceFactoryGaugeScreenState.java",
        ["List<BigItemStack> inputConfig()", "BigItemStack outputConfig()"],
    )
    require(
        args.fluid_129 / "src/main/java/com/yision/fluidlogistics/api/packager/PackageResourceDisplay.java",
        ["String baseUnit();", "factoryPanelRestockPolicy", "maxRequestPerBatch"],
    )

    cfc_screen = (
        args.factory_controller
        / "src/main/java/io/github/nbcss/createfactorycontroller/content/gui/screen/recipe/ConfigureRecipeScreen.java"
    )
    require(
        cfc_screen,
        [
            "private static final int PANEL_W = 200, PANEL_H = 184;",
            "private static final int PROMISE_LIMIT_X = 92, PROMISE_LIMIT_W = 42;",
            "private static final int OUTPUT_X = 160, OUTPUT_Y = 48;",
            "private static final int MULTIPLIER_X = 64, MULTIPLIER_Y = 87, MULTIPLIER_W = 64, MULTIPLIER_H = 8;",
            "private static final int ARROW_ANIM_X = 140, ARROW_ANIM_Y = 47;",
            "int panelX, panelY;",
            "int outputCount = 1;",
            "int craftBatch = 1;",
            "int maxRequestMultiplier = 1;",
            "private int promiseLimitState = -1;",
            "boolean fluidMode = false;",
            "final List<VirtualComponentPosition> inputConnections",
            "final List<Integer> inputTotals",
            "structuralMultiplierCap()",
            "shownIntervalSeconds()",
            "setRequestInterval(",
            "layoutInputSlots(",
            "isFluidConn(",
            "ingredientOf(",
            "slotsUsedExcept(",
            "maxItemOutput()",
            "maxCraftBatch()",
            "maxRequestMultiplier = button == 1 ? 1 : structuralMultiplierCap();",
            "promiseLimitByAddress = !promiseLimitByAddress;",
        ],
    )
    require(
        args.factory_controller
        / "src/main/java/io/github/nbcss/createfactorycontroller/content/component/gauge/VirtualGaugeBehaviour.java",
        [
            "public static final int FLUID_INGREDIENT_CAP_MB = 90_000;",
            "public static final int FLUID_OUTPUT_CAP_MB = 10_000;",
            "public static final int MAX_CRAFT_OUTPUT = 64;",
        ],
    )

    require(
        args.self_root
        / "shared/src/main/java/com/nstut/createprecisecontrols/mixin/FactoryPanelScreenMixin.java",
        ["Screen.hasControlDown()", "fluidlogistics$restockThresholdInput"],
    )
    require(
        args.self_root
        / "shared/src/main/java/com/nstut/createprecisecontrols/mixin/compat/factorycontroller/ConfigureRecipeScreenMixin.java",
        ["Screen.hasControlDown()", "getVirtualGaugeConstant"],
    )
    require(
        args.self_root
        / "shared/src/main/java/com/nstut/createprecisecontrols/mixin/compat/fluidlogistics/ResourceFactoryGaugeScreenMixin.java",
        ["Screen.hasControlDown()", "ScrollInputAccessor"],
    )

    print("Optional-addon compatibility source contracts verified.")


if __name__ == "__main__":
    main()
