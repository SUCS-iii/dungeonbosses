package com.brutalbosses;

import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.CSpriteRenderer;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.VanillaParticleMessage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;

@Environment(EnvType.CLIENT)
public class BrutalBossesClient implements ClientModInitializer
{

    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(ModEntities.THROWN_ITEMC, manager -> new CSpriteRenderer(manager, Minecraft.getInstance().getItemRenderer(), 1.0f, true));

        PayloadTypeRegistry.playS2C().register(BossCapMessage.TYPE, StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossCapMessage().read(byteBuf)));
        PayloadTypeRegistry.playS2C().register(BossOverlayMessage.TYPE, StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossOverlayMessage().read(byteBuf)));
        PayloadTypeRegistry.playS2C().register(BossTypeSyncMessage.TYPE, StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new BossTypeSyncMessage().read(byteBuf)));
        PayloadTypeRegistry.playS2C().register(VanillaParticleMessage.TYPE, StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new VanillaParticleMessage().read(byteBuf)));

        ClientPlayNetworking.registerGlobalReceiver(BossCapMessage.TYPE, (msg, context) -> context.client().execute(catchErrorsFor(() -> msg.handle(context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(BossOverlayMessage.TYPE, (msg, context) -> context.client().execute(catchErrorsFor(() -> msg.handle(context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(BossTypeSyncMessage.TYPE, (msg, context) -> context.client().execute(catchErrorsFor(() -> msg.handle(context.client()))));
        ClientPlayNetworking.registerGlobalReceiver(VanillaParticleMessage.TYPE, (msg, context) -> context.client().execute(catchErrorsFor(() -> msg.handle(context.client()))));
    }

    private Runnable catchErrorsFor(final Runnable runnable)
    {
        return () -> {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                BrutalBosses.LOGGER.warn("error during packet:", e);
            }
        };
    }
}
