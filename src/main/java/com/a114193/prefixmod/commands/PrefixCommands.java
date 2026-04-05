package com.a114193.prefixmod.commands;

import com.a114193.prefixmod.PrefixMod;
import com.a114193.prefixmod.tab.PrefixTeams;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

/**
 * Registers all {@code /prefix} sub-commands.
 * All sub-commands require permission level&nbsp;3 (operator).
 */
public class PrefixCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                CommandManager.literal("prefix")
                    .requires(src -> src.hasPermissionLevel(3))

                    // /prefix set <nick> <prefix...>
                    .then(CommandManager.literal("set")
                        .then(CommandManager.argument("nick", StringArgumentType.word())
                            .then(CommandManager.argument("prefix", StringArgumentType.greedyString())
                                .executes(PrefixCommands::cmdSet))))

                    // /prefix clear <nick>
                    .then(CommandManager.literal("clear")
                        .then(CommandManager.argument("nick", StringArgumentType.word())
                            .executes(PrefixCommands::cmdClear)))

                    // /prefix get <nick>
                    .then(CommandManager.literal("get")
                        .then(CommandManager.argument("nick", StringArgumentType.word())
                            .executes(PrefixCommands::cmdGet)))

                    // /prefix list
                    .then(CommandManager.literal("list")
                        .executes(PrefixCommands::cmdList))

                    // /prefix reload
                    .then(CommandManager.literal("reload")
                        .executes(PrefixCommands::cmdReload))

                    // /prefix tab enable <bool>
                    // /prefix tab title <text...>
                    // /prefix tab subtitle <text...>
                    .then(CommandManager.literal("tab")
                        .then(CommandManager.literal("enable")
                            .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .executes(PrefixCommands::cmdTabEnable)))
                        .then(CommandManager.literal("title")
                            .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(PrefixCommands::cmdTabTitle)))
                        .then(CommandManager.literal("subtitle")
                            .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(PrefixCommands::cmdTabSubtitle))))

                    // /prefix chat enable <bool>
                    .then(CommandManager.literal("chat")
                        .then(CommandManager.literal("enable")
                            .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .executes(PrefixCommands::cmdChatEnable))))
            )
        );
    }

    // -----------------------------------------------------------------------
    // Command implementations
    // -----------------------------------------------------------------------

    private static int cmdSet(CommandContext<ServerCommandSource> ctx) {
        String nick   = StringArgumentType.getString(ctx, "nick");
        String prefix = StringArgumentType.getString(ctx, "prefix");

        PrefixMod.prefixStorage.setPrefix(nick, prefix);

        // Immediately apply the new team to the player if they are online
        ServerPlayerEntity player = onlinePlayer(ctx, nick);
        if (player != null) {
            PrefixTeams.applyTeam(ctx.getSource().getServer(), player, prefix);
        }

        ctx.getSource().sendFeedback(
            () -> Text.literal("§aSet prefix for §e" + nick + "§a: " + PrefixTeams.colorize(prefix)),
            false);
        return 1;
    }

    private static int cmdClear(CommandContext<ServerCommandSource> ctx) {
        String nick = StringArgumentType.getString(ctx, "nick");
        PrefixMod.prefixStorage.clearPrefix(nick);

        ServerPlayerEntity player = onlinePlayer(ctx, nick);
        if (player != null) {
            PrefixTeams.applyTeam(ctx.getSource().getServer(), player, "");
        }

        ctx.getSource().sendFeedback(
            () -> Text.literal("§aCleared prefix for §e" + nick), false);
        return 1;
    }

    private static int cmdGet(CommandContext<ServerCommandSource> ctx) {
        String nick   = StringArgumentType.getString(ctx, "nick");
        String prefix = PrefixMod.prefixStorage.getPrefix(nick);
        if (prefix.isEmpty()) {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§e" + nick + "§7 has no prefix."), false);
        } else {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§7Prefix for §e" + nick + "§7: " + PrefixTeams.colorize(prefix)),
                false);
        }
        return 1;
    }

    private static int cmdList(CommandContext<ServerCommandSource> ctx) {
        Map<String, String> all = PrefixMod.prefixStorage.getAll();
        if (all.isEmpty()) {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§7No prefixes configured."), false);
        } else {
            StringBuilder sb = new StringBuilder("§7--- Prefix list ---");
            all.forEach((nick, pref) ->
                sb.append("\n§e").append(nick).append("§7: ").append(PrefixTeams.colorize(pref)));
            ctx.getSource().sendFeedback(() -> Text.literal(sb.toString()), false);
        }
        return 1;
    }

    private static int cmdReload(CommandContext<ServerCommandSource> ctx) {
        PrefixMod.reload();
        ctx.getSource().sendFeedback(
            () -> Text.literal("§aPrefixMod config reloaded."), false);
        return 1;
    }

    private static int cmdTabEnable(CommandContext<ServerCommandSource> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        PrefixMod.config.setTabEnabled(value);
        ctx.getSource().sendFeedback(
            () -> Text.literal("§aTab header/footer " + (value ? "§2enabled" : "§cdisabled") + "§a."),
            false);
        return 1;
    }

    private static int cmdTabTitle(CommandContext<ServerCommandSource> ctx) {
        String text = StringArgumentType.getString(ctx, "text");
        PrefixMod.config.setTabTitle(text);
        ctx.getSource().sendFeedback(
            () -> Text.literal("§aTab title set to: " + PrefixTeams.colorize(text)), false);
        return 1;
    }

    private static int cmdTabSubtitle(CommandContext<ServerCommandSource> ctx) {
        String text = StringArgumentType.getString(ctx, "text");
        PrefixMod.config.setTabSubtitle(text);
        ctx.getSource().sendFeedback(
            () -> Text.literal("§aTab subtitle set to: " + PrefixTeams.colorize(text)), false);
        return 1;
    }

    private static int cmdChatEnable(CommandContext<ServerCommandSource> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        PrefixMod.config.setChatEnabled(value);
        ctx.getSource().sendFeedback(
            () -> Text.literal("§aChat formatting " + (value ? "§2enabled" : "§cdisabled") + "§a."),
            false);
        return 1;
    }

    // -----------------------------------------------------------------------

    private static ServerPlayerEntity onlinePlayer(CommandContext<ServerCommandSource> ctx, String nick) {
        try {
            return ctx.getSource().getServer().getPlayerManager().getPlayer(nick);
        } catch (Exception e) {
            return null;
        }
    }
}
