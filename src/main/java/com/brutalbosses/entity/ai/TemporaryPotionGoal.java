package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import com.brutalbosses.entity.ModEntities;
import com.brutalbosses.entity.thrownentity.ThrownItemEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies a temporary potion and displays a banner to give a hint at its MobEffects
 */
public class TemporaryPotionGoal extends Goal
{
    public static ResourceLocation ID = ResourceLocation.tryParse("brutalbosses:temppotions");

    private final Mob              mob;
    private       TempPotionParams params;
    private       LivingEntity     target = null;

    public TemporaryPotionGoal(Mob mob, final IAIParams params)
    {
        this.params = (TempPotionParams) params;
        this.mob = mob;
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return params.healthPhaseCheck.test(mob);
        }
        else
        {
            return false;
        }
    }

    public void stop()
    {
        this.target = null;
        for (final Tuple<Holder<MobEffect>, Integer> potion : params.potions)
        {
            final MobEffectInstance MobEffectInstance = mob.getEffect(potion.getA());
            if (MobEffectInstance != null && MobEffectInstance.getDuration() < params.duration)
            {
                mob.removeEffect(potion.getA());
            }
        }
    }

    private int ticksToNextUpdate = 0;

    public void tick()
    {
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        ticksToNextUpdate = (int) params.interval;

        for (final Tuple<Holder<MobEffect>, Integer> potion : params.potions)
        {
            mob.addEffect(new MobEffectInstance(potion.getA(), params.duration, potion.getB()));
        }

        if (params.item != null)
        {
            final ThrownItemEntity item = (ThrownItemEntity) ModEntities.THROWN_ITEMC.create(mob.level());
            item.setPos(mob.getX(), mob.getY(), mob.getZ());
            mob.level().addFreshEntity(item);
            item.startRiding(mob, true);
            ((IOnProjectileHit) item).setMaxLifeTime(mob.level().getGameTime() + params.duration);
            item.setItem(params.item);
            item.setScale(params.visibleitemsize);
        }

        mob.level().playSound(null,
          mob.getX(),
          mob.getY(),
          mob.getZ(),
          SoundEvents.WANDERING_TRADER_DRINK_POTION,
          mob.getSoundSource(),
          1.0F,
          1.0F);

        double d0 = (double) (-Mth.sin(mob.getYRot() * ((float) Math.PI / 180)));
        double d1 = (double) Mth.cos(mob.getYRot() * ((float) Math.PI / 180));
        if (mob.level() instanceof ServerLevel)
        {
            ((ServerLevel) mob.level()).sendParticles(ParticleTypes.CLOUD,
              mob.getX() + d0,
              mob.getY(0.5D),
              mob.getZ() + d1,
              20,
              d0,
              0.0D,
              d1,
              0.0D);
        }
    }

    public static class TempPotionParams extends IAIParams.DefaultParams
    {
        private int                                     duration = 100;
        private float                                   interval = 200;
        private List<Tuple<Holder<MobEffect>, Integer>> potions  = new ArrayList<>();
        private ItemStack                       item            = null;
        private float                           visibleitemsize = 2.0f;

        public TempPotionParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        public static final String POTION_DURATION = "duration";
        public static final String COOLDOWN        = "interval";
        public static final String POTIONS         = "potions";
        public static final String ITEM            = "visibleitem";
        public static final String ITEMSIZE        = "visibleitemsize";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);

            if (jsonElement.has(POTION_DURATION))
            {
                duration = jsonElement.get(POTION_DURATION).getAsInt();
            }

            if (jsonElement.has(COOLDOWN))
            {
                interval = jsonElement.get(COOLDOWN).getAsFloat();
            }

            if (jsonElement.has(ITEMSIZE))
            {
                visibleitemsize = jsonElement.get(ITEMSIZE).getAsFloat();
            }

            if (jsonElement.has(ITEM))
            {
                try
                {
                    item = ItemStack.CODEC.parse(NbtOps.INSTANCE, TagParser.parseTag(jsonElement.get(ITEM).getAsString())).getOrThrow();
                    ;
                }
                catch (CommandSyntaxException e)
                {
                    BrutalBosses.LOGGER.warn("Could not parse item of: " + jsonElement.get(ITEM).getAsString(), e);
                    throw new UnsupportedOperationException();
                }
            }

            if (jsonElement.has(POTIONS))
            {
                potions = new ArrayList<>();
                for (Map.Entry<String, JsonElement> data : jsonElement.get(POTIONS).getAsJsonObject().entrySet())
                {
                    potions.add(new Tuple<>(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.tryParse(data.getKey())).get(), data.getValue().getAsInt()));
                }
            }

            return this;
        }
    }
}