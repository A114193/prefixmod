package com.a114193.prefixmod;

import com.a114193.prefixmod.chat.ChatFormatter;
import com.a114193.prefixmod.commands.PrefixCommands;
import com.a114193.prefixmod.config.ModConfig;
import com.a114193.prefixmod.config.PrefixStorage;
import com.a114193.prefixmod.tab.PrefixTeams;
import com.a114193.prefixmod.tab.TabUpdater;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixMod implements ModInitializer {

    public static final String MOD_ID = "prefixmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig     config;
    public static PrefixStorage prefixStorage;

    private static MinecraftServer server;
    private static TabUpdater      tabUpdater;

    @Override
    public void onInitialize() {
        config        = new ModConfig();
        prefixStorage = new PrefixStorage();
        tabUpdater    = new TabUpdater();

        config.load();
        prefixStorage.load();

        // Register all /prefix commands
        PrefixCommands.register();

        // Register chat formatter
        ChatFormatter.register();

        // Track the server instance as soon as it starts
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);

        // Player join: apply team + track session start
        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            server = s;
            String name = handler.player.getName().getString();
            PrefixTeams.applyTeamToPlayer(s, handler.player);
            tabUpdater.trackJoin(name);
        });

        // Player leave: remove session tracking
        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> {
            String name = handler.player.getName().getString();
            tabUpdater.trackLeave(name);
        });

        // Tick: update TAB header/footer once per second
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            server = s;
            tabUpdater.onTick(s);
        });

        LOGGER.info("PrefixMod initialized. Config: config/prefixmod/");
    }

    /** Returns the current server instance (null before first start). */
    public static MinecraftServer getServer() {
        return server;
    }

    /**
     * Reload config + prefixes from disk and re-apply all teams.
     * Called by {@code /prefix reload}.
     */
    public static void reload() {
        config.load();
        prefixStorage.load();
        if (server != null) {
            PrefixTeams.reapplyAll(server);
        }
        LOGGER.info("PrefixMod reloaded.");
    }
}
