package com.nstut.createprecisecontrols.mixin;

import net.createmod.catnip.gui.AbstractSimiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses Create/Catnip layout state from the class that actually declares it.
 * Subclass mixins must not shadow these inherited fields directly.
 */
@Mixin(value = AbstractSimiScreen.class, remap = false)
public interface AbstractSimiScreenAccessor {
    @Accessor("guiLeft")
    int createprecisecontrols$getGuiLeft();

    @Accessor("guiTop")
    int createprecisecontrols$getGuiTop();

    @Accessor("windowWidth")
    int createprecisecontrols$getWindowWidth();

    @Accessor("windowHeight")
    int createprecisecontrols$getWindowHeight();
}