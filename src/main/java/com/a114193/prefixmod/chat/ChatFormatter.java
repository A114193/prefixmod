package com.a114193.prefixmod.chat;

import com.a114193.prefixmod.PrefixMod;
import com.a114193.prefixmod.tab.PrefixTeams;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Intercepts outgoing chat messages on the server side.
 * When chat formatting is enabled the original (signed) message is cancelled
 * and a custom plaintext broadcast is sent to all players so that the
 * configured chatFormat (with {prefix}, {name}, {msg} placeholders) is shown.
 */
public class ChatFormatter {

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (!PrefixMod.config.isChatEnabled()) {
                return true; // let vanilla handle it
            }

            String prefix     = PrefixMod.prefixStorage.getPrefix(sender.getName().getString());
            String playerName = sender.getName().getString();
            String msg        = message.getContent().getString();

            // If prefix is present add a trailing space so it looks like "[TAG] Name"
            // without requiring the operator to manually include one in every prefix.
            String prefixWithSpace = prefix.isEmpty() ? "" : prefix + " ";

            String formatted = PrefixMod.config.getChatFormat()
                .replace("{prefix}", prefixWithSpace)
                .replace("{name}",   playerName)
                .replace("{msg}",    msg);

            formatted = PrefixTeams.colorize(formatted);

            Text formattedText = Text.literal(formatted);

            // Broadcast our custom message to every online player
            for (ServerPlayerEntity p : sender.getServer().getPlayerManager().getPlayerList()) {
                p.sendMessage(formattedText, false);
            }

            // Also echo to the server console
            PrefixMod.LOGGER.info("[Chat] {}", formatted.replace("§", "&"));

            return false; // cancel the original signed message
        });
    }
}
