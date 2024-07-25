package com.brutalbosses.entity.capability;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossType;
import com.brutalbosses.entity.BossTypeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BossCapability {

    private BossType bossEntry = null;
    private Entity entity = null;
    private ResourceLocation lootTable = null;
    private BlockPos spawnPos = BlockPos.ZERO;

    private final static String KEY = "bbosspath";
    private final static String NAMESPACE = "bbossnamesp";
    private final static String LTKEY = "bbossltk";
    private final static String LTNAMESPACE = "bbossltn";
    private final static String XSPAWN = "spX";
    private final static String YSPAWN = "spY";
    private final static String ZSPAWN = "spZ";
    private final static String SHOWBOSSBAR = "shb";

    public static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BrutalBosses.MOD_ID, "bosscap");

    public BossCapability(final Entity entity) {
        this.entity = entity;
    }

    public BossCapability() {
    }

    public CompoundTag serializeNBT() {
        if (bossEntry == null) {
            return new CompoundTag();
        }

        final CompoundTag compoundNbt = new CompoundTag();
        compoundNbt.putString(NAMESPACE, bossEntry.getID().getNamespace());
        compoundNbt.putString(KEY, bossEntry.getID().getPath());

        if (lootTable != null) {
            compoundNbt.putString(LTKEY, lootTable.getNamespace());
            compoundNbt.putString(LTNAMESPACE, lootTable.getPath());
        }

        if (spawnPos != BlockPos.ZERO) {
            compoundNbt.putInt(XSPAWN, spawnPos.getX());
            compoundNbt.putInt(YSPAWN, spawnPos.getY());
            compoundNbt.putInt(ZSPAWN, spawnPos.getZ());
        }

        compoundNbt.putBoolean(SHOWBOSSBAR, bossEntry.showBossBar());
        return compoundNbt;
    }

    public void deserializeNBT(final Tag nbt) {
        if (nbt == null) {
            return;
        }

        final CompoundTag compoundNbt = (CompoundTag) nbt;

        if (!compoundNbt.contains(NAMESPACE) || !compoundNbt.contains(KEY)) {
            return;
        }

        final String nameSpace = compoundNbt.getString(NAMESPACE);
        final String path = compoundNbt.getString(KEY);

        final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(nameSpace, path);

        if (compoundNbt.contains(LTKEY) && compoundNbt.contains(LTNAMESPACE)) {
            lootTable = ResourceLocation.fromNamespaceAndPath(compoundNbt.get(LTKEY).getAsString(), compoundNbt.get(LTNAMESPACE).getAsString());
        }

        if (compoundNbt.contains(XSPAWN)) {
            spawnPos = new BlockPos(compoundNbt.getInt(XSPAWN), compoundNbt.getInt(YSPAWN), compoundNbt.getInt(ZSPAWN));
        }

        bossEntry = BossTypeManager.instance.bosses.get(id);
        if (bossEntry == null) {
            BrutalBosses.LOGGER.warn("Could not find boss for id:" + id);
        } else {
            if (!entity.level().isClientSide())
            {
                bossEntry.initForEntity((LivingEntity) entity);
            }
            else
            {
                bossEntry.initForClientEntity((LivingEntity) entity);
            }

            bossEntry.setBossBar(compoundNbt.getBoolean(SHOWBOSSBAR));
        }
    }

    public void setBossType(final BossType bossEntry) {
        this.bossEntry = bossEntry;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(final BlockPos pos) {
        spawnPos = pos;
    }

    public ResourceLocation getLootTable() {
        return lootTable;
    }

    public void setLootTable(final ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    public boolean isBoss() {
        return bossEntry != null;
    }

    public Entity getEntity() {
        return entity;
    }

    public BossType getBossType() {
        return bossEntry;
    }
}
