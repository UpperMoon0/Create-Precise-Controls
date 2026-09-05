package com.nstut.createprecisecontrols.mixin;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import com.nstut.createprecisecontrols.compat.fluidlogistics.FluidLogisticsCompat;
import com.nstut.createprecisecontrols.platform.ValueSettingsSender;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds exact entry to Create's Factory Gauge target-amount value board. */
@Mixin(value = ValueSettingsScreen.class, remap = false)
public abstract class ValueSettingsScreenMixin extends Screen {
    protected ValueSettingsScreenMixin(Component title) {
        super(title);
    }

    @Shadow private BlockPos pos;
    @Shadow private ValueSettingsBoard board;
    @Shadow private int netId;
    @Shadow private boolean iconMode;
    @Shadow public abstract ValueSettings getClosestCoordinate(int mouseX, int mouseY);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && isFactoryGauge() && supportsExactTarget()) {
            ValueSettings hovered = getClosestCoordinate((int) mouseX, (int) mouseY);
            int row = hovered.row();
            int initial = hovered.value();
            int max = maxSafeValue(row);
            Component unit = row >= 0 && row < board.rows().size()
                    ? board.rows().get(row) : Component.empty();
            Minecraft.getInstance().setScreen(new ExactAmountScreen(null,
                    Component.translatable("createprecisecontrols.screen.factory_target", unit),
                    initial, 0, max,
                    value -> ValueSettingsSender.send(pos, row, value, netId)));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Create stores the gauge target as an int and later multiplies stack-mode values by the
     * filtered item's stack size. Cap the typed value so the resulting demand stays at or below
     * Create's own BigItemStack.INF sentinel instead of allowing integer overflow.
     */
    private int maxSafeValue(int row) {
        FactoryPanelBehaviour behaviour = findFactoryPanelBehaviour();
        if (behaviour == null) return board.maxValue();
        int unitSize = 1;
        if (row != 0 && !behaviour.getFilter().isEmpty())
            unitSize = Math.max(1, behaviour.getFilter().getMaxStackSize());
        return Math.max(board.maxValue(), (BigItemStack.INF - 1) / unitSize);
    }

    private FactoryPanelBehaviour findFactoryPanelBehaviour() {
        Level level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(pos) instanceof SmartBlockEntity blockEntity)) return null;
        for (BlockEntityBehaviour behaviour : blockEntity.getAllBehaviours())
            if (behaviour instanceof FactoryPanelBehaviour panel && panel.netId() == netId)
                return panel;
        return null;
    }

    /**
     * FluidLogistics 1.2.6 converts ValueSettings row/value pairs into resource amounts inside
     * FactoryPanelBehaviour. Sending a raw mB number through Create's ValueSettingsPacket would
     * therefore be semantically wrong. Its newer dedicated resource screen is handled separately.
     */
    private boolean supportsExactTarget() {
        FactoryPanelBehaviour behaviour = findFactoryPanelBehaviour();
        return behaviour == null || !FluidLogisticsCompat.isResource(behaviour.getFilter());
    }

    private boolean isFactoryGauge() {
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockState(pos).is(AllBlocks.FACTORY_GAUGE.get());
    }

    @Inject(method = "renderWindow", at = @At("TAIL"), remap = false)
    private void createprecisecontrols$renderHint(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                                   CallbackInfo ci) {
        if (!isFactoryGauge() || !supportsExactTarget()) return;
        AbstractSimiScreenAccessor layout = (AbstractSimiScreenAccessor) (Object) this;
        int additionalHeight = iconMode ? 46 : 33;
        Component hint = Component.translatable("createprecisecontrols.hint.middle_click_exact");
        int centerX = layout.createprecisecontrols$getGuiLeft() + layout.createprecisecontrols$getWindowWidth() / 2;
        int y = layout.createprecisecontrols$getGuiTop() + layout.createprecisecontrols$getWindowHeight()
                + additionalHeight - 15;
        graphics.drawCenteredString(font, hint, centerX, y, 0x777777);
    }
}
