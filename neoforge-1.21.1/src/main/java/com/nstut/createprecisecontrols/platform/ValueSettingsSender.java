package com.nstut.createprecisecontrols.platform;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Version bridge for Create 1.21.1's Catnip networking service. */
public final class ValueSettingsSender {
    private ValueSettingsSender() {}

    public static void send(BlockPos pos, int row, int value, int netId) {
        CatnipServices.NETWORK.sendToServer(new ValueSettingsPacket(
                pos, row, value, null, null, Direction.UP, AllKeys.ctrlDown(), netId));
    }
}