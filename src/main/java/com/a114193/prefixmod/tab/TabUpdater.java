package com.a114193.prefixmod.tab;

import com.a114193.prefixmod.PrefixMod;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends a custom TAB header/footer packet to every online player once per
 * second (every 20 server ticks).  Tracks per-player join timestamps so the
 * session duration placeholder ({session}) is accurate.
 */
public class TabUpdater {

    private final Map<String, Long> joinTimes = new HashMap<>();
    private int tickCounter = 0;

    // -----------------------------------------------------------------------

    public void trackJoin(String playerName) {
        joinTimes.put(playerName, System.currentTimeMillis());
    }

    public void trackLeave(String playerName) {
        joinTimes.remove(playerName);
    }

    public void onTick(MinecraftServer server) {
        if (++tickCounter < 20) return;
        tickCounter = 0;

        if (!PrefixMod.config.isTabEnabled()) return;

        String header = buildHeader();
        int online = server.getCurrentPlayerCount();
        int max    = server.getMaxPlayerCount();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String footer = buildFooter(player, online, max);
            player.networkHandler.sendPacket(
                new PlayerListHeaderS2CPacket(
                    Text.literal(colorize(header)),
                    Text.literal(colorize(footer))
                )
            );
        }
    }

    // -----------------------------------------------------------------------

    private String buildHeader() {
        return PrefixMod.config.getTabTitle()
            + "\n"
            + PrefixMod.config.getTabSubtitle();
    }

    private String buildFooter(ServerPlayerEntity player, int online, int max) {
        int  ping    = player.networkHandler.latency;
        long elapsed = System.currentTimeMillis()
                       - joinTimes.getOrDefault(player.getName().getString(), System.currentTimeMillis());
        String session = formatDuration(elapsed);

        return PrefixMod.config.getTabFooterFormat()
            .replace("{ping}",    String.valueOf(ping))
            .replace("{online}",  String.valueOf(online))
            .replace("{max}",     String.valueOf(max))
            .replace("{session}", session);
    }

    private static String formatDuration(long ms) {
        long secs  = ms / 1000;
        long hours = secs / 3600;
        long mins  = (secs % 3600) / 60;
        long s     = secs % 60;
        return String.format("%02d:%02d:%02d", hours, mins, s);
    }

    private static String colorize(String text) {
        return text == null ? "" : text.replace('&', '§');
    }
}
