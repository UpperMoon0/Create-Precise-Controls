package com.nstut.createprecisecontrols.mixin.compat.fluidlogistics;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat.ResourceAmountSpec;
import com.nstut.createprecisecontrols.mixin.AbstractSimiScreenAccessor;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * Compatibility for FluidLogistics 1.2.9+'s dedicated ResourceFactoryGaugeScreen.
 *
 * <p>FluidLogistics 1.2.6 edits Create's FactoryPanelScreen directly and is handled by
 * {@code FactoryPanelScreenMixin}. Newer FluidLogistics versions moved resource gauges to their
 * own screen/state object, so this optional mixin follows that source layout without creating a
 * hard dependency on the addon.</p>
 */
@Pseudo
@Mixin(targets = "com.yision.fluidlogistics.content.logistics.factoryGauge.client.ResourceFactoryGaugeScreen",
        remap = false)
public abstract class ResourceFactoryGaugeScreenMixin {
    private static final int INPUT_GRID_X = 68;
    private static final int INPUT_GRID_Y = 28;
    private static final int CELL_STEP = 20;
    private static final int CELL_SIZE = 16;
    private static final int OUTPUT_X = 160;
    private static final int OUTPUT_Y = 48;
    private static final int RIGHT_BUTTON = 1;
    private static final int MIDDLE_BUTTON = 2;
    private static boolean reflectionFailureLogged;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void createprecisecontrols$openFluidLogisticsAmount(double mouseX, double mouseY, int button,
                                                                 CallbackInfoReturnable<Boolean> cir) {
        if (button != RIGHT_BUTTON && button != MIDDLE_BUTTON) return;

        try {
            Object self = this;
            Screen screen = (Screen) self;

            // The current FluidLogistics resource screen exposes several raw integer ScrollInputs.
            // MMB can type those exactly; use each widget's own min/max instead of duplicating policy.
            if (button == MIDDLE_BUTTON && openResourceScrollInput(self, screen, mouseX, mouseY)) {
                cir.setReturnValue(true);
                return;
            }

            if (getBoolean(self, "emptyFilter") || getBoolean(self, "restocker")) return;

            AbstractSimiScreenAccessor layout = (AbstractSimiScreenAccessor) self;
            int x = layout.createprecisecontrols$getGuiLeft();
            int y = layout.createprecisecontrols$getGuiTop();
            Object state = getField(self, "state");

            @SuppressWarnings("unchecked")
            List<BigItemStack> inputs = (List<BigItemStack>) invoke(state, "inputConfig");
            for (int i = 0; i < inputs.size(); i++) {
                int inputX = x + INPUT_GRID_X + (i % 3 * CELL_STEP);
                int inputY = y + INPUT_GRID_Y + (i / 3 * CELL_STEP);
                if (!inside(mouseX, mouseY, inputX, inputY, CELL_SIZE, CELL_SIZE)) continue;

                BigItemStack input = inputs.get(i);
                if (input.stack.isEmpty()) return;
                openRecipeAmount(screen, input, true);
                cir.setReturnValue(true);
                return;
            }

            if (inside(mouseX, mouseY, x + OUTPUT_X, y + OUTPUT_Y, CELL_SIZE, CELL_SIZE)) {
                BigItemStack output = (BigItemStack) invoke(state, "outputConfig");
                if (output.stack.isEmpty()) return;
                openRecipeAmount(screen, output, false);
                cir.setReturnValue(true);
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logReflectionFailure(ex);
        }
    }

    private static boolean openResourceScrollInput(Object self, Screen screen, double mouseX, double mouseY)
            throws ReflectiveOperationException {
        String[] fields = {"targetAmountInput", "restockThresholdInput", "additionalStockInput", "promiseLimitInput"};
        for (String fieldName : fields) {
            Object value = getField(self, fieldName);
            if (!(value instanceof ScrollInput input) || !input.isMouseOver(mouseX, mouseY)) continue;

            int min = getIntField(input, "min");
            int maxInclusive = getIntField(input, "max") - 1;
            int current = input.getState();
            Component title = switch (fieldName) {
                case "targetAmountInput" -> resourceTitle(self, "createprecisecontrols.screen.resource_target");
                case "restockThresholdInput" -> resourceTitle(self, "createprecisecontrols.screen.restock_threshold");
                case "additionalStockInput" -> resourceTitle(self, "createprecisecontrols.screen.additional_stock");
                case "promiseLimitInput" -> Component.translatable("createprecisecontrols.screen.promise_limit");
                default -> Component.translatable("createprecisecontrols.screen.exact_value");
            };

            Object behaviour = getField(self, "behaviour");
            String setter = switch (fieldName) {
                case "targetAmountInput" -> "fluidlogistics$setTargetAmount";
                case "restockThresholdInput" -> "fluidlogistics$setRestockThreshold";
                case "additionalStockInput" -> "fluidlogistics$setAdditionalStock";
                case "promiseLimitInput" -> "fluidlogistics$setPromiseLimit";
                default -> null;
            };
            open(screen, title, current, min, maxInclusive, newValue -> {
                input.setState(newValue);
                if (setter != null) invokeUnchecked(behaviour, setter, newValue);
            });
            return true;
        }
        return false;
    }

    private static Component resourceTitle(Object self, String key) throws ReflectiveOperationException {
        Object behaviourObject = getField(self, "behaviour");
        if (behaviourObject instanceof FactoryPanelBehaviour behaviour) {
            Optional<ResourceAmountSpec> spec = FluidLogisticsCompat.recipeAmountSpec(behaviour.getFilter());
            if (spec.isPresent()) return Component.translatable(key, spec.get().baseUnit());
        }
        return Component.translatable(key, "units");
    }

    private static void openRecipeAmount(Screen screen, BigItemStack stack, boolean ingredient) {
        Optional<ResourceAmountSpec> resource = FluidLogisticsCompat.recipeAmountSpec(stack.stack);
        int maximum = resource.map(ResourceAmountSpec::maxRequestPerBatch).orElse(64);
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
        open(screen, title, stack.count, 1, maximum, value -> stack.count = value);
    }

    private static void open(Screen parent, Component title, int current, int min, int max, IntConsumer consumer) {
        Minecraft.getInstance().setScreen(new ExactAmountScreen(parent, title, current, min, max, consumer));
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static boolean getBoolean(Object target, String name) throws ReflectiveOperationException {
        return (boolean) getField(target, name);
    }

    private static int getIntField(Object target, String name) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.getInt(target);
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

    private static Object invoke(Object target, String name, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name, args);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
    private static void invokeUnchecked(Object target, String name, Object... args) {
        try {
            invoke(target, name, args);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Method findMethod(Class<?> type, String name, Object[] args) throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void logReflectionFailure(Throwable ex) {
        if (reflectionFailureLogged) return;
        reflectionFailureLogged = true;
        System.err.println("[Create: Precise Controls] FluidLogistics compatibility failed: " + ex);
    }
}
