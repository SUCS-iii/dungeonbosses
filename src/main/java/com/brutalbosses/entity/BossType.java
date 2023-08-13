package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ai.IAIParams;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.event.EventHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;

import static com.brutalbosses.entity.CustomAttributes.REMOVESPELLAI;

/**
 * Represents one boss type
 */
public class BossType {

    /**
     * The bosses entity used
     */
    private final EntityType entityToUse;

    /**
     * Additional data for the boss entity on creation
     */
    private CompoundTag creationData = null;

    /**
     * ID of this boss set
     */
    private final ResourceLocation id;

    private ImmutableMap<MobEffect, Integer> potionMobEffects = ImmutableMap.of();
    private ImmutableMap<EquipmentSlot, ItemStack> gearMap = ImmutableMap.of();
    private ImmutableMap<ResourceLocation, IAIParams> aiData = ImmutableMap.of();
    private ImmutableMap<Attribute, Float> attributes = ImmutableMap.of();
    private ImmutableMap<ResourceLocation, Integer> spawnTables = ImmutableMap.of();
    private ImmutableMap<String, Float> customAttributes = ImmutableMap.of();

    /**
     * Name desc
     */
    private String desc;
    private float scale = 1.0f;
    private int experienceDropped = 1;
    private int itemLootCount = 3;
    private boolean showBossBar = true;
    private boolean nameVisible = true;
    private boolean protectsTreasure = true;

    public BossType(final EntityType entityToUse, final ResourceLocation id) {
        this.entityToUse = entityToUse;
        this.id = id;
    }

    /**
     * Creates a new boss entity of this type
     *
     * @param world
     * @return
     */
    public Mob createBossEntity(final Level world) {
        final Entity entity = entityToUse.create(world);

        if (entity instanceof AbstractVillager) {
            // Init empty offers to avoid offers creating maps during worldgen
            ((AbstractVillager) entity).offers = new MerchantOffers();
        }

        if (creationData != null) {
            if (creationData.contains("Pos")) {
                entity.load(creationData);
            } else {
                if (creationData.contains("ForgeCaps", 10) && entity instanceof IEntityCapReader) {
                    ((IEntityCapReader) entity).readCapsFrom(creationData.getCompound("ForgeCaps"));
                }
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).readAdditionalSaveData(creationData);
                }
            }
        }

        if (!(entity instanceof BossCapEntity)) {
            BrutalBosses.LOGGER.warn("Not supported boss entity:" + entityToUse);
            return null;
        }

        ((BossCapEntity) entity).setBossCap(new BossCapability(entity));
        ((BossCapEntity) entity).getBossCap().setBossType(this);
        initForEntity((Mob) entity);
        return (Mob) entity;
    }

    /**
     * Init for client entities
     *
     * @param entity
     */
    public void initForClientEntity(final LivingEntity entity) {
        if (entity instanceof CustomEntityRenderData) {
            ((CustomEntityRenderData) entity).setVisualScale(scale);
            ((CustomEntityRenderData) entity).setDimension(entityToUse.getDimensions().scale(scale));
        }
    }

    /**
     * Init for server entities
     *
     * @param boss
     */
    public void initForEntity(final LivingEntity boss) {
        initGear(boss);
        initStats(boss);
        initAI(boss);
        if (boss instanceof Mob) {
            ((Mob) boss).setPersistenceRequired();
            if (boss.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE)) {
                // ((Mob) boss).restrictTo(boss.getCapability(BOSS_CAP).orElse(null).getSpawnPos(), (int) boss.getAttribute(Attributes.FOLLOW_RANGE).getValue());
            }
        }

        boss.setCustomName(Component.literal(desc));
        if (nameVisible) {
            boss.setCustomNameVisible(true);
        }

        if (protectsTreasure) {
            EventHandler.protectedBlocks.put(((BossCapEntity) boss).getBossCap().getSpawnPos(), boss.getUUID());
        }
    }

    /**
     * Inits stats and MobEffects
     *
     * @param boss
     */
    private void initStats(final LivingEntity boss) {
        final float healthPct = boss.getHealth() / boss.getMaxHealth();

        for (Map.Entry<Attribute, Float> attributeEntry : attributes.entrySet()) {
            if (boss.getAttributes().hasAttribute(attributeEntry.getKey())) {
                if (attributeEntry.getKey() == Attributes.MAX_HEALTH || attributeEntry.getKey() == Attributes.ATTACK_DAMAGE) {
                    boss.getAttribute(attributeEntry.getKey()).setBaseValue(attributeEntry.getValue() * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier);
                } else {
                    boss.getAttribute(attributeEntry.getKey()).setBaseValue(attributeEntry.getValue());
                }
            } else {
                BrutalBosses.LOGGER.debug(
                        "Boss:" + id.toString() + " Attribute: " + attributeEntry.getKey().getDescriptionId() + " is not applicable to: " +
                                entityToUse);
            }
        }

        boss.setHealth(boss.getMaxHealth() * healthPct);

        for (final Map.Entry<MobEffect, Integer> MobEffectEntry : potionMobEffects.entrySet()) {
            boss.getActiveEffectsMap().put(MobEffectEntry.getKey(), new MobEffectInstance(MobEffectEntry.getKey(), 10000000, MobEffectEntry.getValue()));
        }
    }

    /**
     * Inits gear used
     *
     * @param boss
     */
    private void initGear(final LivingEntity boss) {
        if (boss instanceof Mob) {
            for (final Map.Entry<EquipmentSlot, ItemStack> gearEntry : gearMap.entrySet()) {
                boss.setItemSlot(gearEntry.getKey(), gearEntry.getValue());
            }
        }
    }

    /**
     * Inits AI used
     *
     * @param boss
     */
    private void initAI(final LivingEntity boss) {
        if (customAttributes.containsKey(REMOVESPELLAI) && boss instanceof Mob) {
            for (final Iterator<WrappedGoal> iterator = ((Mob) boss).goalSelector.getAvailableGoals().iterator(); iterator.hasNext(); ) {
                final WrappedGoal goal = iterator.next();
                if (goal.getGoal() instanceof SpellcasterIllager.SpellcasterUseSpellGoal) {
                    goal.stop();
                    iterator.remove();
                }
            }
        }

        for (final Map.Entry<ResourceLocation, IAIParams> data : aiData.entrySet()) {
            if (BossTypeManager.instance.aiCreatorRegistry.containsKey(data.getKey())) {
                BossTypeManager.instance.aiCreatorRegistry.get(data.getKey()).accept(boss, data.getValue());
            }
        }
    }

    public ResourceLocation getID() {
        return id;
    }

    /**
     * Sets the Potion MobEffects
     *
     * @param potionMobEffects
     */
    public void setMobEffects(final ImmutableMap<MobEffect, Integer> potionMobEffects) {
        this.potionMobEffects = potionMobEffects;
    }

    /**
     * Set the gear to use
     *
     * @param gearMap
     */
    public void setGear(final ImmutableMap<EquipmentSlot, ItemStack> gearMap) {
        this.gearMap = gearMap;
    }

    /**
     * Set the attributes to apply
     *
     * @param attributes
     */
    public void setAttributes(final ImmutableMap<Attribute, Float> attributes) {
        this.attributes = attributes;
    }

    /**
     * sets the list of AI's
     *
     * @param aiData
     */
    public void setAIData(final ImmutableMap<ResourceLocation, IAIParams> aiData) {
        this.aiData = aiData;
    }

    /**
     * Gets the entity type used for this boss
     *
     * @return
     */
    public EntityType getEntityType() {
        return entityToUse;
    }

    /**
     * Sets the desc after name
     */
    public void setDesc(final String desc) {
        this.desc = desc;
    }

    /**
     * Sets the spawn loottables of chests
     *
     * @param spawnData
     */
    public void setSpawnTable(final ImmutableMap<ResourceLocation, Integer> spawnData) {
        this.spawnTables = spawnData;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BossType) {
            return id.equals(((BossType) other).id);
        }

        return false;
    }

    /**
     * Get the spawn tables
     *
     * @return
     */
    public ImmutableMap<ResourceLocation, Integer> getSpawnTables() {
        return spawnTables;
    }

    /**
     * Set the visual scale
     *
     * @param scale
     */
    public void setVisualScale(final float scale) {
        this.scale = scale;
    }

    /**
     * Get the visual scale
     *
     * @return
     */
    public float getVisualScale() {
        return scale;
    }

    /**
     * Get the configured json value of this attribute
     *
     * @return
     */
    public float getCustomAttributeValueOrDefault(final String attributeID, float defaultValue) {
        return customAttributes.getOrDefault(attributeID, defaultValue);
    }

    /**
     * Set the custom attributes
     *
     * @param customAttributes
     */
    public void setCustomAttributes(final ImmutableMap<String, Float> customAttributes) {
        this.customAttributes = customAttributes;
    }

    /**
     * Get the exp dropped
     *
     * @return
     */
    public int getExperienceDrop() {
        return experienceDropped;
    }

    /**
     * Get the exp dropped
     *
     * @return
     */
    public void setExperienceDrop(final int exp) {
        experienceDropped = exp;
    }

    /**
     * Get the params for an AI
     *
     * @param id
     * @return
     */
    public IAIParams getAIParams(final ResourceLocation id) {
        return aiData.get(id);
    }

    public int getItemLootCount() {
        return itemLootCount;
    }

    public void setItemLootCount(final int itemLootCount) {
        this.itemLootCount = itemLootCount;
    }

    /**
     * Serialize nbt to client
     *
     * @return
     */
    public CompoundTag serializeToClient() {
        final CompoundTag CompoundTag = new CompoundTag();
        CompoundTag.putString("id", id.toString());
        CompoundTag.putString("etype", Registry.ENTITY_TYPE.getKey(entityToUse).toString());
        CompoundTag.putFloat("scale", scale);


        return CompoundTag;
    }

    /**
     * Serialize nbt to client
     *
     * @return
     */
    public static BossType deserializeAtClient(final CompoundTag CompoundTag) {
        final ResourceLocation id = ResourceLocation.tryParse(CompoundTag.getString("id"));
        final ResourceLocation entity = ResourceLocation.tryParse(CompoundTag.getString("etype"));
        final EntityType type = Registry.ENTITY_TYPE.get(entity);

        if (type == null) {
            return null;
        }

        final BossType bossType = new BossType(type, id);
        bossType.setVisualScale(CompoundTag.getFloat("scale"));
        return bossType;
    }

    public void setBossBar(final boolean show) {
        this.showBossBar = show;
    }

    public boolean showBossBar() {
        return showBossBar;
    }

    public void setEntityNBT(final CompoundTag entityNBT) {
        creationData = entityNBT;
    }

    public void setNameVisible(final boolean visible) {
        nameVisible = visible;
    }

    public void setProtectsTreasure(final boolean protectsTreasure) {
        this.protectsTreasure = protectsTreasure;
    }
}
