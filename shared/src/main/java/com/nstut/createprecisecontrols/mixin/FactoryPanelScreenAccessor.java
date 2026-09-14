package com.nstut.createprecisecontrols.mixin;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes Create's existing Factory Gauge save path to compatibility code. */
@Mixin(value = FactoryPanelScreen.class, remap = false)
public interface FactoryPanelScreenAccessor {
    @Invoker("sendIt")
    void createprecisecontrols$sendIt(FactoryPanelPosition toRemove, boolean clearPromises);
}
