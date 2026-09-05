package com.nstut.createprecisecontrols.mixin;

import com.nstut.createprecisecontrols.client.ExactAmountScreen;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FactoryPanelScreen.class, remap = false)
public abstract class FactoryPanelScreenMixin {
    @Shadow private List<BigItemStack> inputConfig;
    @Shadow private BigItemStack outputConfig;
    @Shadow private boolean craftingActive;
    @Shadow private boolean restocker;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void createprecisecontrols$openExactAmount(double mouseX, double mouseY, int button,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (button != 1 || craftingActive) return;

        Screen screen = (Screen) (Object) this;
        AbstractSimiScreen simi = (AbstractSimiScreen) (Object) this;
        int x = simi.getGuiLeft();
        int y = simi.getGuiTop();

        for (int i = 0; i < inputConfig.size(); i++) {
            int inputX = x + 68 + (i % 3 * 20);
            int inputY = y + 28 + (i / 3 * 20);
            if (!inside(mouseX, mouseY, inputX, inputY, 16, 16)) continue;
            BigItemStack stack = inputConfig.get(i);
            if (stack.stack.isEmpty()) return;
            Minecraft.getInstance().setScreen(new ExactAmountScreen(screen,
                    Component.literal("Exact ingredient amount"), stack.count, 1, 64,
                    value -> stack.count = value));
            cir.setReturnValue(true);
            return;
        }

        if (restocker) return;
        int outputX = x + 160;
        int outputY = y + 48;
        if (inside(mouseX, mouseY, outputX, outputY, 16, 16)) {
            Minecraft.getInstance().setScreen(new ExactAmountScreen(screen,
                    Component.literal("Exact output amount"), outputConfig.count, 1, 64,
                    value -> outputConfig.count = value));
            cir.setReturnValue(true);
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
