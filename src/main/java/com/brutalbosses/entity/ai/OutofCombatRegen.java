package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.ResourceLocation;

/**
 * Simply chases the target at the required distance
 */
public class OutofCombatRegen extends Goal
{
    private final MobEntity mob;
    private final float     amount;
    private       int       combatTimer = 0;

    public OutofCombatRegen(MobEntity mob)
    {
        final BossCapability cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        amount = ((CombatParams) cap.getBossType().getAIParams(ID)).amount;
        this.mob = mob;
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            combatTimer = 20 * 30;
        }
        else if (combatTimer > 0)
        {
            combatTimer--;
        }
        else
        {
            combatTimer = 20;
            mob.heal(amount);
        }

        return false;
    }

    public void stop()
    {
    }

    public void tick()
    {

    }

    public static        ResourceLocation ID     = new ResourceLocation("brutalbosses:aftercombatregen");
    private static final String           AMOUNT = "amount";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        final CombatParams params = new CombatParams();
        if (jsonElement.has(AMOUNT))
        {
            params.amount = jsonElement.get(AMOUNT).getAsFloat();
        }

        return params;
    }

    private static class CombatParams implements IAIParams
    {
        private float amount = 2.0f;

        private CombatParams()
        {
        }
    }
}