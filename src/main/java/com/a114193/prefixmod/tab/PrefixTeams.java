package com.a114193.prefixmod.tab;

import com.a114193.prefixmod.PrefixMod;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Manages scoreboard teams whose names start with {@code pm_}.
 * Each unique coloured-prefix string gets its own team; the team's display
 * prefix is set to {@code "<colour-codes> "} so it appears in the TAB list.
 * Non-pm_ teams are never touched.
 */
public class PrefixTeams {

    private static final String TEAM_PREFIX = "pm_";

    // -----------------------------------------------------------------------

    /** Apply the stored prefix for {@code player} (called on join or reload). */
    public static void applyTeamToPlayer(MinecraftServer server, ServerPlayerEntity player) {
        String rawPrefix = PrefixMod.prefixStorage.getPrefix(player.getName().getString());
        applyTeam(server, player, rawPrefix);
    }

    /**
     * Assign {@code player} to the pm_ team that matches {@code rawPrefix}.
     * If the prefix is empty the player is removed from any pm_ team.
     */
    public static void applyTeam(MinecraftServer server, ServerPlayerEntity player, String rawPrefix) {
        String playerName = player.getName().getString();
        Scoreboard sb = server.getScoreboard();

        // Remove from current pm_ team (if any)
        AbstractTeam currentTeam = sb.getPlayerTeam(playerName);
        if (currentTeam instanceof Team current && current.getName().startsWith(TEAM_PREFIX)) {
            sb.removePlayerFromTeam(playerName, current);
        }

        if (rawPrefix == null || rawPrefix.isEmpty()) {
            return;
        }

        String coloured = colorize(rawPrefix);
        Team team = getOrCreateTeam(sb, coloured);
        sb.addPlayerToTeam(playerName, team);
    }

    /** Re-apply teams to all currently online players (used on reload). */
    public static void reapplyAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            applyTeamToPlayer(server, player);
        }
    }

    // -----------------------------------------------------------------------

    /** Find an existing pm_ team with {@code coloured} as prefix, or create one. */
    private static Team getOrCreateTeam(Scoreboard sb, String coloured) {
        Collection<Team> teams = sb.getTeams();
        for (Team t : teams) {
            if (!t.getName().startsWith(TEAM_PREFIX)) continue;
            // The prefix stored on the team is "coloured " (trailing space)
            String stored = t.getPrefix().getString();
            if (stored.equals(coloured + " ") || stored.equals(coloured)) {
                return t;
            }
        }
        // Create a fresh team
        String name = unusedTeamName(sb);
        Team team = sb.addTeam(name);
        team.setPrefix(Text.literal(coloured + " "));
        return team;
    }

    private static String unusedTeamName(Scoreboard sb) {
        int i = 0;
        String name;
        do {
            name = TEAM_PREFIX + i++;
        } while (sb.getTeam(name) != null);  // getTeam returns AbstractTeam, null if absent
        return name;
    }

    /** Replace {@code &} with {@code §} so Minecraft renders the colour codes. */
    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace('&', '§');
    }
}
