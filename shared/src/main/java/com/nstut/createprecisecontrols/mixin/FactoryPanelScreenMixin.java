package com.nstut.createprecisecontrols.mixin;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat.ResourceAmountSpec;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
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
    @Shadow private FactoryPanelBehaviour behaviour;

    @Inject(method = {"mouseClicked", "m_6375_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void createprecisecontrols$openExactAmount(double mouseX, double mouseY, int button,
                                                        CallbackInfoReturnable<Boolean> cir) {
        // Create's ingredient click handler does not inspect the mouse button at all. Reserve a
        // modifier gesture so ordinary LMB/RMB continue to execute Create/addon behavior unchanged.
        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT || !Screen.hasControlDown()) return;

        Screen screen = (Screen) (Object) this;

        // FluidLogistics 1.2.6 injects these ScrollInputs directly into FactoryPanelScreen.
        // Commit through Create's existing sendIt path so FluidLogistics' sendIt hook emits its
        // own setting packet instead of us manufacturing addon network state.
        if (openLegacyFluidLogisticsControl(screen, mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (craftingActive || restocker) return;

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

    private boolean openLegacyFluidLogisticsControl(Screen screen, double mouseX, double mouseY) {
        String[] fields = {
                "fluidlogistics$restockThresholdInput",
                "fluidlogistics$additionalStockInput",
                "fluidlogistics$promiseLimitInput"
        };
        for (String fieldName : fields) {
            ScrollInput input = findOptionalScrollInput(this, fieldName);
            if (input == null || !input.isMouseOver(mouseX, mouseY)) continue;

            ScrollInputAccessor range = (ScrollInputAccessor) (Object) input;
            int min = range.createprecisecontrols$getMin();
            int maxInclusive = range.createprecisecontrols$getMax() - 1;
            Component title = legacyFluidTitle(fieldName);
            Minecraft.getInstance().setScreen(new ExactAmountScreen(
                    screen, title, input.getState(), min, maxInclusive, value -> {
                        input.setState(value);
                        ((FactoryPanelScreenAccessor) (Object) this)
                                .createprecisecontrols$sendIt(null, false);
                    }));
            return true;
        }
        return false;
    }

    private Component legacyFluidTitle(String fieldName) {
        String unit = FluidLogisticsCompat.recipeAmountSpec(behaviour.getFilter())
                .map(ResourceAmountSpec::baseUnit)
                .orElse("units");
        return switch (fieldName) {
            case "fluidlogistics$restockThresholdInput" ->
                    Component.translatable("createprecisecontrols.screen.restock_threshold", unit);
            case "fluidlogistics$additionalStockInput" ->
                    Component.translatable("createprecisecontrols.screen.additional_stock", unit);
            case "fluidlogistics$promiseLimitInput" ->
                    Component.translatable("createprecisecontrols.screen.promise_limit");
            default -> Component.translatable("createprecisecontrols.screen.exact_value");
        };
    }

    private static ScrollInput findOptionalScrollInput(Object target, String name) {
        try {
            Field field = findField(target.getClass(), name);
            field.setAccessible(true);
            Object value = field.get(target);
            return value instanceof ScrollInput input ? input : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
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
