package com.brutalbosses.mixin;

import com.brutalbosses.entity.BossSpawnHandler;
import com.brutalbosses.world.RegionAwareTE;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(RandomizableContainerBlockEntity.class)
public class LockableLootTileEntityMixin implements RegionAwareTE
{
    private boolean                            spawnedBoss = false;
    private WeakReference<ServerLevelAccessor> region      = new WeakReference<>(null);

    @Inject(method = "setLootTable", at = @At("RETURN"))
    private void onSetLoot(final ResourceKey<LootTable> resourceKey, final CallbackInfo ci)
    {
        final ServerLevelAccessor world = region.get();
        if (world != null && !spawnedBoss)
        {
            spawnedBoss = true;
            BossSpawnHandler.onChestPlaced(world, (RandomizableContainerBlockEntity) (Object) this);
        }

        region.clear();
    }

    @Inject(method = "applyImplicitComponents", at = @At("RETURN"))
    private void onLoadLoot(final BlockEntity.DataComponentInput dataComponentInput, final CallbackInfo ci)
    {
            final ServerLevelAccessor world = region.get();
            if (world != null && !spawnedBoss)
            {
                spawnedBoss = true;
                BossSpawnHandler.onChestPlaced(world, (RandomizableContainerBlockEntity) (Object) this);
            }

            region.clear();
    }

    @Override
    public void setRegion(final ServerLevelAccessor region)
    {
        this.region = new WeakReference<>(region);
    }
}
