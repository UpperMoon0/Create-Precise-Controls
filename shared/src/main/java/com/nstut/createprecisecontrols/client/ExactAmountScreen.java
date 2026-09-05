package com.nstut.createprecisecontrols.client;

import com.nstut.createprecisecontrols.ExactAmount;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;

public final class ExactAmountScreen extends Screen {
    private final Screen parent;
    private final int initialValue;
    private final int min;
    private final int max;
    private final IntConsumer onConfirm;
    private EditBox amountBox;
    private Component error = CommonComponents.EMPTY;

    public ExactAmountScreen(Screen parent, Component title, int initialValue, int min, int max, IntConsumer onConfirm) {
        super(title);
        this.parent = parent;
        this.initialValue = initialValue;
        this.min = min;
        this.max = max;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        amountBox = new EditBox(font, centerX - 70, centerY - 12, 140, 20, title);
        amountBox.setMaxLength(10);
        amountBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        amountBox.setValue(Integer.toString(initialValue));
        addRenderableWidget(amountBox);
        setInitialFocus(amountBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> confirm())
                .bounds(centerX - 70, centerY + 16, 68, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX + 2, centerY + 16, 68, 20).build());
    }

    private void confirm() {
        try {
            int value = ExactAmount.parseAndClamp(amountBox.getValue(), min, max);
            onConfirm.accept(value);
            if (minecraft != null) minecraft.setScreen(parent);
        } catch (NumberFormatException ex) {
            error = Component.literal("Enter a whole number from " + min + " to " + max);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x88000000);
        graphics.fill(width / 2 - 86, height / 2 - 54, width / 2 + 86, height / 2 + 50, 0xEE202020);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 42, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Range: " + min + " - " + max), width / 2, height / 2 - 27, 0xA0A0A0);
        if (error != CommonComponents.EMPTY)
            graphics.drawCenteredString(font, error, width / 2, height / 2 + 40, 0xFF5555);
    }
}
