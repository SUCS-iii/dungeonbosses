package com.brutalbosses.entity.ai;

import com.brutalbosses.entity.capability.BossCapability;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Simply chases the target at the required distance
 */
public class ChasingGoal extends Goal
{
    public static ResourceLocation ID = new ResourceLocation("brutalbosses:chasetarget");

    private final MobEntity    mob;
    private       float        chaseDist;
    private       LivingEntity target = null;

    public ChasingGoal(MobEntity mob)
    {
        final BossCapability cap = mob.getCapability(BossCapability.BOSS_CAP).orElse(null);
        chaseDist = ((ChaseParams) cap.getBossType().getAIParams(ID)).chasedistance;
        chaseDist = chaseDist * chaseDist;
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean canUse()
    {
        final LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive())
        {
            this.target = target;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void stop()
    {
        this.target = null;
    }

    private int ticksToNextUpdate = 0;

    public void tick()
    {
        mob.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        if (--ticksToNextUpdate > 0)
        {
            return;
        }

        double distSqr = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());

        ticksToNextUpdate = (int) Math.max(4, (distSqr / 100));

        if (distSqr <= (double) this.chaseDist && this.mob.getSensing().canSee(this.target))
        {
            this.mob.getNavigation().stop();
        }
        else
        {
            final Path path = mob.getNavigation().getPath();
            if (path != null)
            {
                final PathPoint endNode = path.getEndNode();
                if (endNode != null)
                {
                    final BlockPos endPos = new BlockPos(endNode.x, endNode.y, endNode.z);
                    final double endPosDist = this.target.distanceToSqr(endPos.getX(), endPos.getY(), endPos.getZ());
                    if (endPosDist > chaseDist || !this.mob.getSensing().canSee(this.target))
                    {
                        this.mob.getNavigation().moveTo(this.target, 1.0f);
                    }
                }
            }
            else
            {
                this.mob.getNavigation().moveTo(this.target, 1.0f);
            }
        }
    }

    public static final String CHASE_DIST = "chasedistance";

    /**
     * Parses params for this AI
     *
     * @param jsonElement
     * @return
     */
    public static IAIParams parse(final JsonObject jsonElement)
    {
        final ChaseParams params = new ChaseParams();
        if (jsonElement.has(CHASE_DIST))
        {
            params.chasedistance = jsonElement.get(CHASE_DIST).getAsFloat();
        }

        return params;
    }

    private static class ChaseParams implements IAIParams
    {
        private float chasedistance = 2f;

        private ChaseParams()
        {
        }
    }
}