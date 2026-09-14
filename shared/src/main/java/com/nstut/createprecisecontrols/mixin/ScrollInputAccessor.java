package com.nstut.createprecisecontrols.mixin;

import com.simibubi.create.foundation.gui.widget.ScrollInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the authoritative range already configured on Create ScrollInput widgets. */
@Mixin(value = ScrollInput.class, remap = false)
public interface ScrollInputAccessor {
    @Accessor("min")
    int createprecisecontrols$getMin();

    @Accessor("max")
    int createprecisecontrols$getMax();
}
