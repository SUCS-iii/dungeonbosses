package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * fake message for UI
 */
public class BossOverlayMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BossOverlayMessage> TYPE     =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MOD_ID, "bossoverlay"));
    private             int                                          entityID = -1;

    public BossOverlayMessage(final int entityID)
    {
        this.entityID = entityID;
    }

    public BossOverlayMessage()
    {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(entityID);
    }

    @Override
    public BossOverlayMessage read(final FriendlyByteBuf buffer)
    {
        entityID = buffer.readInt();
        return this;
    }

    @Override
    public void handle(Minecraft client)
    {
        final Entity entity = client.player.level().getEntity(entityID);
        if (entity != null)
        {
            ClientEventHandler.checkEntity(entity);
        }
    }

    @Override
    public ResourceLocation getID()
    {
        return TYPE.id();
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}

