package com.brutalbosses.entity.ai;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.IOnProjectileHit;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.Explosion;

public class ItemThrowAttackGoal extends SimpleRangedAttackGoal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:itemshootgoal");

    public ItemThrowAttackGoal(final MobEntity mob)
    {
        super(mob);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected void doRangedAttack(final ProjectileEntity projectileEntity, final LivingEntity target)
    {
        projectileEntity.remove();

        double xDiff = target.getX() - mob.getX();
        double yDiff = target.getY(0.5D) - (mob.getY() + mob.getEyeHeight() - 0.5);
        double zDiff = target.getZ() - mob.getZ();

        final EnderPearlEntity pearlEntity = new EnderPearlEntity(mob.level, mob);
        pearlEntity.setPos(mob.getX(), mob.getY() + mob.getEyeHeight() - 0.5, mob.getZ());
        pearlEntity.shoot(xDiff, yDiff, zDiff, 0.8F, 3.0F);

        if (!((ItemThrowParams) params).teleport)
        {
            pearlEntity.ownerUUID = null;
            pearlEntity.ownerNetworkId = 0;
        }

        pearlEntity.setItem(((ItemThrowParams) params).item);
        pearlEntity.setNoGravity(true);
        if (pearlEntity instanceof IOnProjectileHit)
        {
            ((IOnProjectileHit) pearlEntity).setMaxLifeTime(mob.level.getGameTime() + 20 * 20);
            ((IOnProjectileHit) pearlEntity).setOnHitAction(rayTraceResult ->
            {
                if (rayTraceResult instanceof EntityRayTraceResult)
                {
                    final Entity hitEntity = ((EntityRayTraceResult) rayTraceResult).getEntity();
                    if (hitEntity instanceof LivingEntity && hitEntity != mob)
                    {
                        if (((ItemThrowParams) params).damage > 0)
                        {
                            hitEntity.hurt(DamageSource.indirectMagic(hitEntity, mob), ((ItemThrowParams) params).damage);
                        }

                        if (((ItemThrowParams) params).lighting)
                        {
                            LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(hitEntity.level);
                            lightningboltentity.moveTo(hitEntity.getX(), hitEntity.getY(), hitEntity.getZ());
                            lightningboltentity.setVisualOnly(false);
                            mob.level.addFreshEntity(lightningboltentity);
                        }

                        if (((ItemThrowParams) params).explode)
                        {
                            hitEntity.level.explode(null,
                              DamageSource.indirectMobAttack((LivingEntity) hitEntity, mob),
                              null,
                              hitEntity.getX(),
                              hitEntity.getY(),
                              hitEntity.getZ(),
                              (float) (1 * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier.get()),
                              false,
                              Explosion.Mode.NONE);
                        }
                    }
                }
                else if (rayTraceResult instanceof BlockRayTraceResult)
                {
                    final BlockPos hitPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
                    if (((ItemThrowParams) params).lighting)
                    {
                        LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(mob.level);
                        lightningboltentity.moveTo(hitPos.getX(), hitPos.getY(), hitPos.getZ());
                        lightningboltentity.setVisualOnly(false);
                        mob.level.addFreshEntity(lightningboltentity);
                    }

                    if (((ItemThrowParams) params).explode)
                    {
                        mob.level.explode(null,
                          DamageSource.indirectMobAttack(mob, mob),
                          null,
                          hitPos.getX(),
                          hitPos.getY(),
                          hitPos.getZ(),
                          (float) (1 * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier.get()),
                          false,
                          Explosion.Mode.NONE);
                    }

                }
            });
        }

        mob.level.addFreshEntity(pearlEntity);
    }

    @Override
    protected ProjectileEntity createProjectile()
    {
        final EnderPearlEntity pearlEntity = new EnderPearlEntity(mob.level, mob);
        pearlEntity.setItem(((ItemThrowParams) params).item);
        return pearlEntity;
    }

    private static final String ITEM      = "item";
    private static final String DAMAGE    = "damage";
    private static final String TELEPORT  = "teleport";
    private static final String LIGHTNING = "lightning";
    private static final String EXPLODE   = "explode";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        final ItemThrowParams params = new ItemThrowParams();
        SimpleRangedAttackGoal.parse(jsonElement, params);

        if (jsonElement.has(ITEM))
        {
            try
            {
                params.item = ItemStack.of(JsonToNBT.parseTag(jsonElement.get(ITEM).getAsString()));
            }
            catch (CommandSyntaxException e)
            {
                BrutalBosses.LOGGER.warn("Could not parse item of: " + jsonElement.get(ITEM).getAsString(), e);
            }
        }

        if (jsonElement.has(DAMAGE))
        {
            params.damage = jsonElement.get(DAMAGE).getAsFloat();
        }

        if (jsonElement.has(TELEPORT))
        {
            params.teleport = true;
        }

        if (jsonElement.has(LIGHTNING))
        {
            params.lighting = true;
        }

        if (jsonElement.has(EXPLODE))
        {
            params.explode = true;
        }

        return params;
    }

    private static class ItemThrowParams extends RangedParams
    {
        private ItemStack item     = Items.ENDER_PEARL.getDefaultInstance();
        private float     damage   = 0;
        private boolean   teleport = false;
        private boolean   lighting = false;
        private boolean   explode  = false;
    }
}