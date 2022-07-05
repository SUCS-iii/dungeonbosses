package com.brutalbosses.network;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public class BossTypeSyncMessage implements IMessage
{
    private Collection<BossType> bossTypes = new HashSet<>();

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
    public void handle(final Supplier<NetworkEvent.Context> contextSupplier)
    {
        if (contextSupplier.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
        {
            BrutalBosses.LOGGER.error("Boss capability message sent to the wrong side!", new Exception());
        }
        else
        {
            final ImmutableMap.Builder<ResourceLocation, BossType> bossTypesImm = ImmutableMap.<ResourceLocation, BossType>builder();
            for (final BossType type : bossTypes)
            {
                bossTypesImm.put(type.getID(), type);
            }

            BossTypeManager.instance.bosses = bossTypesImm.build();
        }

        contextSupplier.get().setPacketHandled(true);
    }
}