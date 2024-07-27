package com.brutalbosses.event;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public class ModEventHandler
{
    @SubscribeEvent
    public static void onConfigChanged(ModConfigEvent event)
    {

    }

    @SubscribeEvent
    public static void registerEntities(final RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE))
        {
            event.register(Registries.ENTITY_TYPE, ThrownItemEntity.ID, () -> ModEntities.THROWN_ITEMC);
        }
    }
}
