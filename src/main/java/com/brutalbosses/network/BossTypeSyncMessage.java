package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class BossTypeSyncMessage implements IMessage, CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BossTypeSyncMessage> TYPE      =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BrutalBosses.MODID, "bosstypes"));
    private             Collection<BossType>                          bossTypes = new HashSet<>();

    public BossTypeSyncMessage(final Collection<BossType> values)
    {
        bossTypes = values;
    }

    public BossTypeSyncMessage()
    {

    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(bossTypes.size());
        for (final BossType type : bossTypes)
        {
            buffer.writeNbt((CompoundTag) type.serializeToClient());
        }
    }

    @Override
    public BossTypeSyncMessage read(final FriendlyByteBuf buffer)
    {
        final int count = buffer.readInt();
        bossTypes = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            final BossType type = BossType.deserializeAtClient(buffer.readNbt());
            if (type != null)
            {
                bossTypes.add(type);
            }
        }
        return this;
    }

    @Override
    public void handle(Player player)
    {
        final ImmutableMap.Builder<ResourceLocation, BossType> bossTypesImm = ImmutableMap.builder();
        for (final BossType type : bossTypes)
        {
            bossTypesImm.put(type.getID(), type);
        }

        BossTypeManager.instance.bosses = bossTypesImm.build();
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
