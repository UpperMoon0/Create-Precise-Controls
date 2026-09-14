#!/usr/bin/env python3
"""Verify the published Create: Factory Controller binaries used by dev runtimes."""

from __future__ import annotations

import argparse
import subprocess
from pathlib import Path

SCREEN_CLASS = "io.github.nbcss.createfactorycontroller.content.gui.screen.recipe.ConfigureRecipeScreen"
GAUGE_CLASS = "io.github.nbcss.createfactorycontroller.content.component.gauge.VirtualGaugeBehaviour"

SCREEN_MARKERS = [
    "int panelX;",
    "int panelY;",
    "int outputCount;",
    "int craftBatch;",
    "int maxRequestMultiplier;",
    "boolean fluidMode;",
    "inputConnections;",
    "inputTotals;",
    "GaugeWorkMode workMode;",
    "private int promiseLimitState;",
    "ingredientOf(",
    "boolean isFluidConn(int);",
    "layoutInputSlots();",
    "layoutInputSlots(int);",
    "int slotsUsedExcept(int);",
    "private int maxCraftBatch();",
    "int maxItemOutput();",
    "int structuralMultiplierCap();",
    "private int shownIntervalSeconds();",
    "private void setRequestInterval(int);",
]

CONSTANT_MARKERS = [
    "public static final int FLUID_INGREDIENT_CAP_MB = 90000;",
    "public static final int FLUID_OUTPUT_CAP_MB = 10000;",
    "public static final int MAX_CRAFT_OUTPUT = 64;",
]


def javap(jar: Path, class_name: str, *, constants: bool = False) -> str:
    if not jar.is_file():
        raise SystemExit(f"missing published compatibility jar: {jar}")
    command = ["javap", "-private"]
    if constants:
        command.append("-constants")
    command += ["-classpath", str(jar), class_name]
    try:
        return subprocess.check_output(command, text=True, stderr=subprocess.STDOUT)
    except (OSError, subprocess.CalledProcessError) as exc:
        output = getattr(exc, "output", "")
        raise SystemExit(f"javap failed for {jar} / {class_name}:\n{output}") from exc


def require(text: str, markers: list[str], label: str) -> None:
    missing = [marker for marker in markers if marker not in text]
    if missing:
        details = "\n".join(f"  - {marker}" for marker in missing)
        raise SystemExit(f"published CFC binary contract changed for {label}:\n{details}")


def verify(jar: Path, label: str, click_method: str) -> None:
    screen = javap(jar, SCREEN_CLASS)
    require(screen, SCREEN_MARKERS + [click_method], f"{label} ConfigureRecipeScreen")
    constants = javap(jar, GAUGE_CLASS, constants=True)
    require(constants, CONSTANT_MARKERS, f"{label} VirtualGaugeBehaviour")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--forge-jar", type=Path, required=True)
    parser.add_argument("--neoforge-jar", type=Path, required=True)
    args = parser.parse_args()

    verify(args.forge_jar, "Forge 1.20.1", "public boolean m_6375_(double, double, int);")
    verify(args.neoforge_jar, "NeoForge 1.21.1", "public boolean mouseClicked(double, double, int);")
    print("Published Create: Factory Controller 1.2.1 binary contracts verified for Forge and NeoForge.")


if __name__ == "__main__":
    main()
