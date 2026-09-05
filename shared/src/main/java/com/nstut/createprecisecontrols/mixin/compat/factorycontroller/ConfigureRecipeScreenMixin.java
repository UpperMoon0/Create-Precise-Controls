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

@Pseudo
@Mixin(targets = "io.github.nbcss.createfactorycontroller.content.gui.screen.recipe.ConfigureRecipeScreen", remap = false)
public abstract class ConfigureRecipeScreenMixin {
    private static final int GRID_X = 68;
    private static final int GRID_Y = 28;
    private static final int CELL_STEP = 20;
    private static final int CELL_SIZE = 16;
    private static final int OUTPUT_X = 160;
    private static final int OUTPUT_Y = 48;
    private static final int FLUID_INGREDIENT_CAP_MB = 90_000;
    private static final int FLUID_OUTPUT_CAP_MB = 64_000;
    private static boolean reflectionFailureLogged;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void createprecisecontrols$openFactoryControllerAmount(double mouseX, double mouseY, int button,
                                                                    CallbackInfoReturnable<Boolean> cir) {
        if (button != 1) return;
        try {
            Object self = this;
            int panelX = getInt(self, "panelX");
            int panelY = getInt(self, "panelY");
            String mode = String.valueOf(getField(self, "workMode"));
            Screen screen = (Screen) self;

            if (!"CRAFTING".equals(mode)
                    && inside(mouseX, mouseY, panelX + OUTPUT_X, panelY + OUTPUT_Y, CELL_SIZE, CELL_SIZE)) {
                int current = getInt(self, "outputCount");
                boolean fluid = getBoolean(self, "fluidMode");
                int max = fluid ? FLUID_OUTPUT_CAP_MB : (int) invoke(self, "maxItemOutput");
                Minecraft.getInstance().setScreen(new ExactAmountScreen(screen,
                        Component.literal(fluid ? "Exact output amount (mB)" : "Exact output amount"),
                        current, 1, max, value -> setIntUnchecked(self, "outputCount", value)));
                cir.setReturnValue(true);
                return;
            }

            if (!"REGULAR".equals(mode)) return;
            int slot = slotAt(mouseX, mouseY, panelX, panelY);
            if (slot < 0) return;

            List<?> slots = (List<?>) invoke(self, "layoutInputSlots");
            if (slot >= slots.size()) return;
            Object inputSlot = slots.get(slot);
            int connectionIndex = (int) invoke(inputSlot, "connectionIndex");

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
                int usedExcept = (int) invoke(self, "slotsUsedExcept", connectionIndex);
                int maxSlots = Math.max(1, 9 - usedExcept);
                max = maxSlots * stackSize;
            }

            int maxValue = max;
            Minecraft.getInstance().setScreen(new ExactAmountScreen(screen,
                    Component.literal(fluid ? "Exact ingredient amount (mB)" : "Exact ingredient amount"),
                    current, 1, maxValue, value -> totals.set(connectionIndex, value)));
            cir.setReturnValue(true);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (!reflectionFailureLogged) {
                reflectionFailureLogged = true;
                System.err.println("[Create: Precise Controls] Create: Factory Controller compatibility failed: " + ex);
            }
        }
    }

    private static int slotAt(double mouseX, double mouseY, int panelX, int panelY) {
        int x = (int) Math.floor(mouseX) - (panelX + GRID_X);
        int y = (int) Math.floor(mouseY) - (panelY + GRID_Y);
        if (x < 0 || x >= 58 || y < 0 || y >= 58) return -1;
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
            } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }

    private static Object invoke(Object target, String name, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name, args);
        method.setAccessible(true);
        return method.invoke(target, args);
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
}
