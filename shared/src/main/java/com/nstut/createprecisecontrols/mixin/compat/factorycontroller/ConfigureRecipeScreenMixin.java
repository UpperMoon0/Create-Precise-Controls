package com.nstut.createprecisecontrols.mixin.compat.factorycontroller;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Optional Create: Factory Controller integration. It is intentionally reflection-backed so CFC is
 * not a compile/runtime dependency of Create: Precise Controls.
 */
@Pseudo
@Mixin(targets = "io.github.nbcss.createfactorycontroller.content.gui.screen.recipe.ConfigureRecipeScreen", remap = false)
public abstract class ConfigureRecipeScreenMixin {
    private static final int PANEL_H = 184;
    private static final int GRID_X = 68;
    private static final int GRID_Y = 28;
    private static final int GRID_SIZE = 58;
    private static final int CELL_STEP = 20;
    private static final int CELL_SIZE = 16;
    private static final int OUTPUT_X = 160;
    private static final int OUTPUT_Y = 48;
    private static final int MULTIPLIER_X = 64;
    private static final int MULTIPLIER_Y = 87;
    private static final int MULTIPLIER_W = 64;
    private static final int MULTIPLIER_H = 8;
    private static final int INTERVAL_X = 140;
    private static final int INTERVAL_Y = 47;
    private static final int INTERVAL_W = 15;
    private static final int INTERVAL_H = 17;
    private static final int PROMISE_LIMIT_X = 92;
    private static final int PROMISE_LIMIT_Y_FROM_BOTTOM = 24;
    private static final int PROMISE_LIMIT_W = 42;
    private static final int PROMISE_LIMIT_H = 16;
    private static final int FLUID_INGREDIENT_CAP_MB = 90_000;
    private static final int FLUID_OUTPUT_CAP_MB = 64_000;
    private static final int MAX_CRAFT_BATCH = 64;
    private static final int MAX_INTERVAL_SECONDS = 60;
    private static final int MAX_PROMISE_LIMIT = 99;
    private static final int TICKS_PER_SECOND = 20;
    private static final int RIGHT_BUTTON = 1;
    private static boolean reflectionFailureLogged;

    @Inject(method = {"mouseClicked", "m_6375_"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void createprecisecontrols$openFactoryControllerAmount(double mouseX, double mouseY, int button,
                                                                    CallbackInfoReturnable<Boolean> cir) {
        // Right-click is the single precise-entry gesture wherever CFC itself has no RMB action.
        if (button != RIGHT_BUTTON) return;

        try {
            Object self = this;
            int panelX = getInt(self, "panelX");
            int panelY = getInt(self, "panelY");
            String mode = String.valueOf(getField(self, "workMode"));
            Screen screen = (Screen) self;

            if (inside(mouseX, mouseY, panelX + MULTIPLIER_X, panelY + MULTIPLIER_Y,
                    MULTIPLIER_W, MULTIPLIER_H)) {
                int current = getInt(self, "maxRequestMultiplier");
                int max = Math.max(1, ((Number) invoke(self, "structuralMultiplierCap")).intValue());
                open(screen, Component.translatable("createprecisecontrols.screen.request_multiplier"),
                        current, 1, max, value -> setIntUnchecked(self, "maxRequestMultiplier", value));
                cir.setReturnValue(true);
                return;
            }

            if (inside(mouseX, mouseY, panelX + INTERVAL_X, panelY + INTERVAL_Y,
                    INTERVAL_W, INTERVAL_H)) {
                int current = ((Number) invoke(self, "shownIntervalSeconds")).intValue();
                open(screen, Component.translatable("createprecisecontrols.screen.request_interval"),
                        current, 1, MAX_INTERVAL_SECONDS,
                        value -> invokeUnchecked(self, "setRequestInterval", value * TICKS_PER_SECOND));
                cir.setReturnValue(true);
                return;
            }

            if (inside(mouseX, mouseY, panelX + PROMISE_LIMIT_X,
                    panelY + PANEL_H - PROMISE_LIMIT_Y_FROM_BOTTOM,
                    PROMISE_LIMIT_W, PROMISE_LIMIT_H)) {
                int current = Math.max(0, getInt(self, "promiseLimitState"));
                open(screen, Component.translatable("createprecisecontrols.screen.promise_limit"),
                        current, 0, MAX_PROMISE_LIMIT,
                        value -> setIntUnchecked(self, "promiseLimitState", value));
                cir.setReturnValue(true);
                return;
            }
            if (inside(mouseX, mouseY, panelX + OUTPUT_X, panelY + OUTPUT_Y, CELL_SIZE, CELL_SIZE)) {
                if ("CRAFTING".equals(mode)) {
                    int current = Math.max(1, getInt(self, "craftBatch"));
                    open(screen, Component.translatable("createprecisecontrols.screen.craft_batch"),
                            current, 1, MAX_CRAFT_BATCH,
                            value -> setIntUnchecked(self, "craftBatch", value));
                } else {
                    int current = getInt(self, "outputCount");
                    boolean fluid = getBoolean(self, "fluidMode");
                    int max = fluid ? FLUID_OUTPUT_CAP_MB : ((Number) invoke(self, "maxItemOutput")).intValue();
                    open(screen,
                            Component.translatable(fluid
                                    ? "createprecisecontrols.screen.fluid_output_amount"
                                    : "createprecisecontrols.screen.output_amount"),
                            current, 1, max,
                            value -> setIntUnchecked(self, "outputCount", value));
                }
                cir.setReturnValue(true);
                return;
            }

            if (!"REGULAR".equals(mode)) return;
            int slot = slotAt(mouseX, mouseY, panelX, panelY);
            if (slot < 0) return;

            List<?> slots = (List<?>) invoke(self, "layoutInputSlots");
            if (slot >= slots.size()) return;
            Object inputSlot = slots.get(slot);
            int connectionIndex = ((Number) invoke(inputSlot, "connectionIndex")).intValue();

            @SuppressWarnings("unchecked")
            List<Integer> totals = (List<Integer>) getField(self, "inputTotals");
            int current = Math.max(1, totals.get(connectionIndex));
            boolean fluid = (boolean) invoke(self, "isFluidConn", connectionIndex);
            int max;
            if (fluid) {
                max = FLUID_INGREDIENT_CAP_MB;
            } else {
                @SuppressWarnings("unchecked")
                List<Object> connections = (List<Object>) getField(self, "inputConnections");
                ItemStack ingredient = (ItemStack) invoke(self, "ingredientOf", connections.get(connectionIndex));
                int stackSize = Math.max(1, ingredient.getMaxStackSize());
                int usedExcept = ((Number) invoke(self, "slotsUsedExcept", connectionIndex)).intValue();
                int maxSlots = Math.max(1, 9 - usedExcept);
                max = maxSlots * stackSize;
            }

            int maxValue = max;
            open(screen,
                    Component.translatable(fluid
                            ? "createprecisecontrols.screen.fluid_ingredient_amount"
                            : "createprecisecontrols.screen.ingredient_amount"),
                    current, 1, maxValue,
                    value -> totals.set(connectionIndex, value));
            cir.setReturnValue(true);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logReflectionFailure(ex);
        }
    }

    private static void open(Screen parent, Component title, int current, int min, int max,
                             java.util.function.IntConsumer consumer) {
        Minecraft.getInstance().setScreen(new ExactAmountScreen(parent, title, current, min, max, consumer));
    }

    private static int slotAt(double mouseX, double mouseY, int panelX, int panelY) {
        int x = (int) Math.floor(mouseX) - (panelX + GRID_X);
        int y = (int) Math.floor(mouseY) - (panelY + GRID_Y);
        if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) return -1;
        int col = x / CELL_STEP;
        int row = y / CELL_STEP;
        return x % CELL_STEP < CELL_SIZE && y % CELL_STEP < CELL_SIZE ? row * 3 + col : -1;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static int getInt(Object target, String name) throws ReflectiveOperationException {
        return ((Number) getField(target, name)).intValue();
    }

    private static boolean getBoolean(Object target, String name) throws ReflectiveOperationException {
        return (boolean) getField(target, name);
    }

    private static void setIntUnchecked(Object target, String name, int value) {
        try {
            Field field = findField(target.getClass(), name);
            field.setAccessible(true);
            field.setInt(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
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
                Class<?>[] parameterTypes = method.getParameterTypes();
                boolean matches = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!compatible(parameterTypes[i], args[i])) {
                        matches = false;
                        break;
                    }
                }
                if (matches) return method;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static boolean compatible(Class<?> parameterType, Object arg) {
        if (arg == null) return !parameterType.isPrimitive();
        if (parameterType.isInstance(arg)) return true;
        return (parameterType == int.class && arg instanceof Integer)
                || (parameterType == boolean.class && arg instanceof Boolean)
                || (parameterType == double.class && arg instanceof Double)
                || (parameterType == long.class && arg instanceof Long);
    }

    private static void logReflectionFailure(Throwable ex) {
        if (reflectionFailureLogged) return;
        reflectionFailureLogged = true;
        System.err.println("[Create: Precise Controls] Create: Factory Controller compatibility failed: " + ex);
    }
}