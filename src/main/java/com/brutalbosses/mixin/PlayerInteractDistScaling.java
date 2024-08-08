package com.brutalbosses.mixin;

import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PlayerInteractDistScaling
{
    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    public void canReach(final CallbackInfoReturnable<Float> cir)
    {
        if (this instanceof BossCapEntity bossCapEntity)
        {
            final BossCapability cap = bossCapEntity.getBossCap();
            if (cap != null && cap.isBoss() && cap.getBossType().getVisualScale() > 1.0f)
            {
                cir.setReturnValue(cap.getBossType().getVisualScale() + 2);
            }
        }
    }
}
