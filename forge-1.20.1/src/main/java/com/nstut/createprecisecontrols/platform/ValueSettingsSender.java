package com.nstut.createprecisecontrols.platform;

import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Version bridge for Create 1.20.1's legacy packet channel. */
public final class ValueSettingsSender {
    private ValueSettingsSender() {}

    public static void send(BlockPos pos, int row, int value, int netId) {
        AllPackets.getChannel().sendToServer(new ValueSettingsPacket(
                pos, row, value, null, null, Direction.UP, AllKeys.ctrlDown(), netId));
    }
}