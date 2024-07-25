package com.brutalbosses.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Network
{

    public static final Network instance = new Network();

    private Network()
    {

    }

    public void sendPacket(final ServerPlayer player, final IMessage msg)
    {
        ServerPlayNetworking.send(player, msg);
    }
}
