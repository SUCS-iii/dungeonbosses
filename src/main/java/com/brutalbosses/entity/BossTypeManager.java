package com.brutalbosses.entity;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.ai.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Manages and holds all boss entries and stores the capability
 */
public class BossTypeManager
{
    public              Map<ResourceLocation, BossType> bosses      = ImmutableMap.of();
    public              Set<ResourceLocation>           entityTypes = ImmutableSet.of();
    public static final BossTypeManager                                        instance          = new BossTypeManager();
    public              Map<ResourceLocation, BiConsumer<Entity, IAIParams>>   aiCreatorRegistry = ImmutableMap.of();
    public              Map<ResourceLocation, Function<JsonObject, IAIParams>> aiParamParsers    = ImmutableMap.of();
    public              ImmutableMap<ResourceLocation, List<BossType>>         lootTableSpawnEntries = ImmutableMap.of();
    private BossTypeManager()
    {


        registerAI(ResourceLocation.tryParse("minecraft:randomwalk"),
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new RandomStrollGoal((PathfinderMob) entity, 0.8d, 20)),
          null);

        registerAI(ResourceLocation.tryParse("minecraft:meleeattack"),
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new MeleeAttackGoal((PathfinderMob) entity, 1.0d, true)),
          null);

        registerAI(ResourceLocation.tryParse("minecraft:crossbow"),
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000,
            new RangedCrossbowAttackGoal<>((Monster & RangedAttackMob & CrossbowAttackMob) entity, 1.0d, 30)),
          null);

        registerAI(MeleeShieldAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2001, new MeleeShieldAttackGoal((Mob) entity, 1.0d)),
          null);

        registerAI(ResourceLocation.tryParse("minecraft:target"),
          (entity, params) -> ((Mob) entity).targetSelector.addGoal(-2000, new NearestAttackableTargetGoal<>((Mob) entity, Player.class, true)),
          null);

        registerAI(LavaRescueGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new LavaRescueGoal((Mob) entity)),
          null);

        registerAI(ChasingGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2001, new ChasingGoal((Mob) entity, params)),
          ChasingGoal.ChaseParams::new);

        registerAI(SmallFireballAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new SmallFireballAttackGoal((Mob) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(WitherSkullAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new WitherSkullAttackGoal((Mob) entity, params)),
          WitherSkullAttackGoal.WitherSkullParams::new);

        registerAI(SnowballAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new SnowballAttackGoal((Mob) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(OutofCombatRegen.ID, (entity, params) -> ((Mob) entity).targetSelector.addGoal(-2000, new OutofCombatRegen((Mob) entity, params)),
          OutofCombatRegen.CombatParams::new);

        registerAI(SpitCobwebGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new SpitCobwebGoal((Mob) entity, params)),
          SimpleRangedAttackGoal.RangedParams::new);

        registerAI(SummonMobsGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new SummonMobsGoal((Mob) entity, params)),
          SummonMobsGoal.SummonParams::new);

        registerAI(WhirlWind.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new WhirlWind((Mob) entity, params)),
          WhirlWind.WhirldWindParams::new);

        registerAI(MeleeHitGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new MeleeHitGoal((Mob) entity, params)),
          MeleeHitGoal.MeleeHitParams::new);

        registerAI(ChargeGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new ChargeGoal((Mob) entity, params)),
          ChargeGoal.ChargeParams::new);

        registerAI(BigFireballAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new BigFireballAttackGoal((Mob) entity, params)),
          BigFireballAttackGoal.RangedParams::new);

        registerAI(ItemThrowAttackGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new ItemThrowAttackGoal((Mob) entity, params)),
          ItemThrowAttackGoal.ItemThrowParams::new);

        registerAI(TemporaryPotionGoal.ID,
          (entity, params) -> ((Mob) entity).goalSelector.addGoal(-2000, new TemporaryPotionGoal((Mob) entity, params)),
          TemporaryPotionGoal.TempPotionParams::new);
    }

    /**
     * Register the AI and adds additional alternative ID ones ending with 1 - 4
     *
     * @param ID
     * @param aiCreator
     * @param paramsParser
     */
    public void registerAI(ResourceLocation ID, BiConsumer<Entity, IAIParams> aiCreator, Function<JsonObject, IAIParams> paramsParser)
    {
        final ImmutableMap.Builder<ResourceLocation, BiConsumer<Entity, IAIParams>> aiRegistry = ImmutableMap.builder();
        final ImmutableMap.Builder<ResourceLocation, Function<JsonObject, IAIParams>> aiSupplier = ImmutableMap.<ResourceLocation, Function<JsonObject, IAIParams>>builder();

        aiRegistry.putAll(this.aiCreatorRegistry);
        aiSupplier.putAll(this.aiParamParsers);

        aiRegistry.put(ID, aiCreator);
        if (paramsParser != null)
        {
            aiSupplier.put(ID, paramsParser);
        }

        for (int i = 1; i < 5; i++)
        {
            final ResourceLocation additionalID = ResourceLocation.fromNamespaceAndPath(ID.getNamespace(), ID.getPath() + i);
            aiRegistry.put(additionalID, aiCreator);
            if (paramsParser != null)
            {
                aiSupplier.put(additionalID, paramsParser);
            }
        }

        this.aiCreatorRegistry = aiRegistry.build();
        this.aiParamParsers = aiSupplier.build();
    }

    /**
     * After reloading data we recalc some stuff
     */
    public void afterLoad()
    {
        final ImmutableSet.Builder<ResourceLocation> entityTypes = ImmutableSet.builder();
        final HashMap<ResourceLocation, List<BossType>> tempSpawns = new HashMap<>();

        for (final BossType bossType : bosses.values())
        {
            entityTypes.add(BuiltInRegistries.ENTITY_TYPE.getKey(bossType.getEntityType()));
            BrutalBosses.LOGGER.info("Loaded boss variant for: " + BuiltInRegistries.ENTITY_TYPE.getKey(bossType.getEntityType()));

            for (final Map.Entry<ResourceLocation, Integer> spawnEntry : bossType.getSpawnTables().entrySet())
            {
                final List<BossType> contained = tempSpawns.computeIfAbsent(spawnEntry.getKey(), loc -> new ArrayList<>());
                for (int i = 0; i < spawnEntry.getValue(); i++)
                {
                    contained.add(bossType);
                }
            }
        }

        this.entityTypes = entityTypes.build();

        final ImmutableMap.Builder<ResourceLocation, List<BossType>> spawnMap = ImmutableMap.builder();
        for (final Map.Entry<ResourceLocation, List<BossType>> entry : tempSpawns.entrySet())
        {
            final ImmutableList.Builder<BossType> bossList = ImmutableList.builder();
            bossList.addAll(entry.getValue());
            spawnMap.put(entry.getKey(), bossList.build());
        }

        this.lootTableSpawnEntries = spawnMap.build();
    }

    /**
     * Check if we have a valid entity type for bosses
     *
     * @param entity
     * @return
     */
    public boolean isValidBossEntity(final Entity entity)
    {
        return entityTypes.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    /**
     * Sets the boss types after load
     *
     * @param bossTypes
     */
    public void setBossTypes(final ImmutableMap<ResourceLocation, BossType> bossTypes)
    {
        this.bosses = bossTypes;
    }
}
