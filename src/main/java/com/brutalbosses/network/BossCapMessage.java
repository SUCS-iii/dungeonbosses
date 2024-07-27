package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class BossCapMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BossCapMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MODID, "bosscap"));
    BossCapability cap = null;

    private int         entityID = -1;
    private CompoundTag nbt      = null;

    public BossCapMessage(final BossCapability cap)
    {
        this.cap = cap;
    }

    public BossCapMessage()
    {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(cap.getEntity().getId());
        buffer.writeNbt((CompoundTag) cap.serializeNBT());
    }

    @Override
    public BossCapMessage read(final FriendlyByteBuf buffer)
    {
        entityID = buffer.readInt();
        nbt = buffer.readNbt();
        return this;
    }

    @Override
    public void handle(final Player player)
    {
        final Entity entity = player.level().getEntity(entityID);
        if (entity instanceof BossCapEntity)
        {
            ((BossCapEntity) entity).setBossCap(new BossCapability(entity));
            ((BossCapEntity) entity).getBossCap().deserializeNBT(nbt);
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