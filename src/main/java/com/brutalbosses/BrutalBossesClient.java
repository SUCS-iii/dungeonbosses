package com.brutalbosses;

import com.brutalbosses.event.ClientEventHandler;
import com.brutalbosses.event.ClientRendererRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class BrutalBossesClient
{
    public static void onInitializeClient(final IEventBus modEventBus)
    {
        NeoForge.EVENT_BUS.register(ClientEventHandler.class);
        modEventBus.register(ClientRendererRegister.class);
    }
}
