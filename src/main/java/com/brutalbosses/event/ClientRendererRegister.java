package com.brutalbosses.event;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientRendererRegister
{
    @SubscribeEvent
    public static void initRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.THROWN_ITEMC,
          manager -> new CSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 1.0f, true));
    }
}
