package com.brutalbosses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface IMessage extends CustomPacketPayload
{
    void write(FriendlyByteBuf buffer);

    IMessage read(FriendlyByteBuf buffer);

    void handle(Player player);

    public ResourceLocation getID();
}
