package com.nstut.createprecisecontrols.mixin;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat.ResourceAmountSpec;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/** Adds exact keyboard entry to Create's Factory Gauge recipe amounts. */
@Mixin(value = FactoryPanelScreen.class, remap = false)
public abstract class FactoryPanelScreenMixin {
    private static final int INPUT_GRID_X = 68;
    private static final int INPUT_GRID_Y = 28;
    private static final int CELL_STEP = 20;
    private static final int CELL_SIZE = 16;
    private static final int OUTPUT_X = 160;
    private static final int OUTPUT_Y = 48;
    private static final int CREATE_RECIPE_AMOUNT_MAX = 64;

    @Shadow private List<BigItemStack> inputConfig;
    @Shadow private BigItemStack outputConfig;
    @Shadow private boolean craftingActive;
    @Shadow private boolean restocker;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void createprecisecontrols$openExactAmount(double mouseX, double mouseY, int button,
                                                        CallbackInfoReturnable<Boolean> cir) {
        // Create uses LMB to remove an ingredient connection. FluidLogistics 1.2.6 leaves
        // RMB free here, so right-click opens exact entry without replacing native actions.
        if (button != 1 || craftingActive || restocker) return;

        Screen screen = (Screen) (Object) this;
        AbstractSimiScreenAccessor layout = (AbstractSimiScreenAccessor) (Object) this;
        int x = layout.createprecisecontrols$getGuiLeft();
        int y = layout.createprecisecontrols$getGuiTop();

        for (int i = 0; i < inputConfig.size(); i++) {
            int inputX = x + INPUT_GRID_X + (i % 3 * CELL_STEP);
            int inputY = y + INPUT_GRID_Y + (i / 3 * CELL_STEP);
            if (!inside(mouseX, mouseY, inputX, inputY, CELL_SIZE, CELL_SIZE)) continue;

            BigItemStack stack = inputConfig.get(i);
            if (stack.stack.isEmpty()) return;
            openRecipeAmount(screen, stack, true);
            cir.setReturnValue(true);
            return;
        }

        int outputX = x + OUTPUT_X;
        int outputY = y + OUTPUT_Y;
        if (inside(mouseX, mouseY, outputX, outputY, CELL_SIZE, CELL_SIZE)) {
            openRecipeAmount(screen, outputConfig, false);
            cir.setReturnValue(true);
        }
    }

    /**
     * FluidLogistics stores package resources in BigItemStack.count too, but its valid range is
     * resource-specific and may be far above Create's item limit. Ask its public display API for
     * the exact unit/cap when present; otherwise retain Create's native 1..64 semantics.
     */
    private static void openRecipeAmount(Screen screen, BigItemStack stack, boolean ingredient) {
        Optional<ResourceAmountSpec> resource = FluidLogisticsCompat.recipeAmountSpec(stack.stack);
        int maximum = resource.map(ResourceAmountSpec::maxRequestPerBatch).orElse(CREATE_RECIPE_AMOUNT_MAX);
        Component title = resource
                .<Component>map(spec -> Component.translatable(
                        ingredient
                                ? "createprecisecontrols.screen.resource_ingredient_amount"
                                : "createprecisecontrols.screen.resource_output_amount",
                        spec.baseUnit()))
                .orElseGet(() -> Component.translatable(
                        ingredient
                                ? "createprecisecontrols.screen.ingredient_amount"
                                : "createprecisecontrols.screen.output_amount"));

        Minecraft.getInstance().setScreen(new ExactAmountScreen(
                screen, title, stack.count, 1, maximum, value -> stack.count = value));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
