package com.brutalbosses.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface IMessage extends CustomPacketPayload
{
    void write(FriendlyByteBuf buffer);

    IMessage read(FriendlyByteBuf buffer);

    void handle(Minecraft client);

    public ResourceLocation getID();
}
