package com.brutalbosses;

import com.brutalbosses.command.CommandSpawnBoss;
import com.brutalbosses.config.CommonConfiguration;
import com.brutalbosses.entity.BossJsonListener;
import com.brutalbosses.entity.ModEntities;
import com.cupboard.config.CupboardConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class BrutalBosses implements ModInitializer
{

    public static final String                              MOD_ID = "brutalbosses";
    public static final Logger                              LOGGER = LogManager.getLogger(MOD_ID);
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random                              rand   = new Random();

    @Override
    public void onInitialize()
    {
        LOGGER.info("Brutal bosses initialized");
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new BossJsonListener());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, commandSelection) ->
        {
            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(MOD_ID);
            dispatcher.register(root.then(new CommandSpawnBoss().build()));
        });

        ModEntities.init();
    }

    public static ResourceLocation id(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
