package com.brutalbosses.event;

import com.brutalbosses.BrutalBosses;
import com.brutalbosses.entity.BossJsonListener;
import com.brutalbosses.entity.BossTypeManager;
import com.brutalbosses.entity.CustomAttributes;
import com.brutalbosses.entity.capability.BossCapEntity;
import com.brutalbosses.entity.capability.BossCapability;
import com.brutalbosses.network.BossCapMessage;
import com.brutalbosses.network.BossOverlayMessage;
import com.brutalbosses.network.BossTypeSyncMessage;
import com.brutalbosses.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.brutalbosses.entity.CustomAttributes.DROP_GEAR;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    /*
    @SubscribeEvent
    public static void entitySizeChange(final EntityEvent.Size event)
    {
        // Scale bb only on client side?
        if (event.getEntity() instanceof CustomEntityRenderData && event.getEntity().level.isClientSide())
        {
            final BossCapability cap = event.getEntity().getCapability(BossTypeManager.BOSS_CAP).orElse(null);
            if (cap != null && cap.isBoss())
            {m
                event.setNewSize(event.getOldSize().scale(((CustomEntityRenderData) event.getEntity()).getVisualScale()));
            }
        }
    }
     */

    public static Map<BlockPos, UUID> protectedBlocks = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerInteract(final PlayerInteractEvent.EntityInteract event)
    {
        if (event.getLevel().isClientSide())
        {
            return;
        }

        if (protectedBlocks.containsKey(event.getPos()))
        {
            final UUID uuid = protectedBlocks.get(event.getPos());

            final Entity boss = ((ServerLevel) event.getLevel()).getEntity(uuid);
            if (boss instanceof LivingEntity)
            {
                if (boss.isAlive())
                {
                    ((LivingEntity) boss).addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60));

                    if (boss instanceof Mob && ((Mob) boss).getTarget() == null)
                    {
                        ((Mob) boss).setTarget(event.getEntity());
                    }

                    ((ServerPlayer) event.getEntity()).sendSystemMessage(Component.translatable("boss.chest.lock",
                      boss.getDisplayName()).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void applyProjectileDamageBoost(final LivingDamageEvent.Pre event)
    {
        if (event.getSource().getEntity() instanceof Player && event.getEntity() instanceof BossCapEntity bossCapEntity)
        {
            final BossCapability cap = bossCapEntity.getBossCap();
            if (cap != null && cap.isBoss())
            {
                Network.instance.sendPacket((ServerPlayer) event.getSource().getEntity(), new BossOverlayMessage(event.getEntity().getId()));
            }
            return;
        }

        if (event.getSource().is(DamageTypes.THROWN) && event.getSource().getEntity() instanceof BossCapEntity bossCapEntity)
        {
            final BossCapability cap = bossCapEntity.getBossCap();
            if (cap != null && cap.isBoss())
            {
                event.setNewDamage((float) ((event.getNewDamage() + cap.getBossType().getCustomAttributeValueOrDefault(CustomAttributes.PROJECTILE_DAMAGE, 0))
                                           * BrutalBosses.config.getCommonConfig().globalDifficultyMultiplier));
            }
        }
    }

    @SubscribeEvent
    public static void playerClickBlockEvent(final PlayerInteractEvent.RightClickBlock event)
    {
        if (!BrutalBosses.config.getCommonConfig().printChestLoottable || event.getLevel().isClientSide())
        {
            return;
        }

        final BlockEntity te = event.getEntity().level().getBlockEntity(event.getPos());
        if (te instanceof RandomizableContainerBlockEntity && ((RandomizableContainerBlockEntity) te).lootTable != null)
        {
            event.getEntity()
              .sendSystemMessage(Component.literal("[Loottable: " + ((RandomizableContainerBlockEntity) te).lootTable + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                  ((RandomizableContainerBlockEntity) te).lootTable.toString()))));
        }
    }

    @SubscribeEvent
    public static void onBossDeath(final LivingDeathEvent event)
    {
        if (!event.getEntity().level().isClientSide() && event.getSource().getEntity() instanceof ServerPlayer && event.getEntity() instanceof BossCapEntity bossCapEntity)
        {
            final BossCapability cap = bossCapEntity.getBossCap();
            if (cap != null && cap.isBoss())
            {
                int exp = cap.getBossType().getExperienceDrop();
                while (exp > 0)
                {
                    int orbValue = ExperienceOrb.getExperienceValue(exp);
                    exp -= orbValue;
                    event.getEntity().level().addFreshEntity(new ExperienceOrb(event.getEntity().level(),
                      event.getEntity().getX(),
                      event.getEntity().getY(),
                      event.getEntity().getZ(),
                      orbValue));
                }

                final int gearDropCount = Math.min(EquipmentSlot.values().length, (int) cap.getBossType().getCustomAttributeValueOrDefault(DROP_GEAR, 0));

                for (int i = 0; i < gearDropCount; i++)
                {
                    final ItemEntity itementity =
                      new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        event.getEntity().getItemBySlot(EquipmentSlot.values()[i]));
                    event.getEntity().level().addFreshEntity(itementity);
                }

                if (cap.getLootTable() != null)
                {
                    final LootParams params = new LootParams.Builder((ServerLevel) event.getEntity().level())
                      .withParameter(LootContextParams.ORIGIN, event.getEntity().position())
                      .withParameter(LootContextParams.THIS_ENTITY, event.getSource().getEntity())
                      .withLuck(((ServerPlayer) event.getSource().getEntity()).getLuck()).create(LootContextParamSets.CHEST);

                    final LootTable loottable = event.getEntity().level().getServer().reloadableRegistries().get().registry(Registries.LOOT_TABLE).get().get(cap.getLootTable());
                    final List<ItemStack> list = loottable.getRandomItems(params);

                    if (list.isEmpty())
                    {
                        return;
                    }

                    for (int i = 0; i < cap.getBossType().getItemLootCount(); i++)
                    {
                        final ItemEntity itementity =
                          new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), list.get(
                            BrutalBosses.rand.nextInt(list.size())));
                        event.getEntity().level().addFreshEntity(itementity);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event)
    {
        event.addListener(BossJsonListener.instance);
    }

    @SubscribeEvent
    public static void onTrack(PlayerEvent.StartTracking event)
    {
        final Entity entity = event.getTarget();
        final Player Player = event.getEntity();

        if (Player instanceof ServerPlayer && entity instanceof BossCapEntity bossCapEntity)
        {
            final BossCapability bossCapability = bossCapEntity.getBossCap();
            if (bossCapability != null && bossCapability.isBoss())
            {
                Network.instance.sendPacket((ServerPlayer) Player, new BossCapMessage(bossCapability));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.getEntity().level.isClientSide && FMLEnvironment.dist == Dist.DEDICATED_SERVER)
        {
            Network.instance.sendPacket((ServerPlayer) event.getEntity(), new BossTypeSyncMessage(BossTypeManager.instance.bosses.values()));
        }
    }
}
