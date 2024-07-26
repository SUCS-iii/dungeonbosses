package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MeleeReachMixin
{
    @Shadow
    protected abstract AABB getAttackBoundingBox();

    @Inject(method = "isWithinMeleeAttackRange", at = @At("RETURN"), cancellable = true)
    private void adjustForBosses(final LivingEntity livingEntity, final CallbackInfoReturnable<Boolean> cir)
    {
        final BossCapability cap = ((BossCapEntity) this).getBossCap();
        if (cap != null && cap.isBoss())
        {
            cir.setReturnValue(this.getAttackBoundingBox()
                                 .inflate(getAttackBoundingBox().getXsize() * (cap.getBossType().getVisualScale() - 1f),
                                   getAttackBoundingBox().getYsize() * (cap.getBossType().getVisualScale() - 1f),
                                   getAttackBoundingBox().getZsize() * (cap.getBossType().getVisualScale() - 1f))
                                 .intersects(livingEntity.getHitbox()));
        }
    }
}
