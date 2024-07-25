package com.brutalbosses.entity.ai;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;

public class WitherSkullAttackGoal extends SimpleRangedAttackGoal
{
    public static final ResourceLocation ID = ResourceLocation.tryParse("brutalbosses:shootwitherskulls");

    public WitherSkullAttackGoal(final Mob mob, final IAIParams params)
    {
        super(mob, params);
    }

    @Override
    protected ResourceLocation getID()
    {
        return ID;
    }

    @Override
    protected Projectile createProjectile()
    {
        WitherSkull witherskullentity = new WitherSkull(mob.level(), mob, Vec3.ZERO);
        return witherskullentity;
    }

    @Override
    protected void doRangedAttack(final Projectile Projectile, final LivingEntity target)
    {
        Projectile.remove(Entity.RemovalReason.DISCARDED);

        double xDiff = target.getX() - Projectile.getX();
        double yDiff = target.getY(0.5D) - Projectile.getY();
        double zDiff = target.getZ() - Projectile.getZ();


        final WitherSkull witherskullentity = new WitherSkull(mob.level(), mob, new Vec3(xDiff, yDiff, zDiff));
        witherskullentity.accelerationPower *= params.speed;
        witherskullentity.setOwner(mob);
        if (((WitherSkullParams) params).dangerous)
        {
            witherskullentity.setDangerous(true);
        }
        witherskullentity.setPos(Projectile.getX(), Projectile.getY(), Projectile.getZ());
        mob.level().addFreshEntity(witherskullentity);
    }

    public static class WitherSkullParams extends RangedParams
    {
        private boolean dangerous = false;

        public WitherSkullParams(final JsonObject jsonData)
        {
            super(jsonData);
            parse(jsonData);
        }

        private static final String DANGEROUS = "dangerous";

        @Override
        public IAIParams parse(final JsonObject jsonElement)
        {
            super.parse(jsonElement);
            if (jsonElement.has(DANGEROUS))
            {
                dangerous = jsonElement.get(DANGEROUS).getAsBoolean();
            }
            return this;
        }
    }
}
