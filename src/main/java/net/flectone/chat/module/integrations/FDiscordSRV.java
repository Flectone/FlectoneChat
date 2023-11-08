package net.flectone.chat.module.integrations;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.builder.MessageBuilder;
import net.flectone.chat.model.advancement.FAdvancement;
import net.flectone.chat.model.damager.PlayerDamager;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.flectone.chat.manager.FileManager.integrations;

/*

DiscordSRV - https://github.com/DiscordSRV/DiscordSRV

Copyright (C) 2016 - 2022 Austin "Scarsz" Shapiro
This file is part of DiscordDRV

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public
License along with this program.  If not, see
<http://www.gnu.org/licenses/gpl-3.0.html>.

 */

public class FDiscordSRV implements Listener, FIntegration {

    public FDiscordSRV() {
        init();
    }

    public void sendDeathMessage(@NotNull Player sender, @NotNull PlayerDamager playerDamager, @NotNull String typeDeath) {
        Entity finalEntity = playerDamager.getFinalEntity();
        Material finalBlock = playerDamager.getFinalBlockDamager();
        Entity killer = playerDamager.getKiller();
        ItemStack killerItem = playerDamager.getKillerItem();

        String message = integrations.getString("DiscordSRV.message.death.type" + typeDeath);

        if (finalEntity != null) message = message
                .replace("<killer>", finalEntity.getName())
                .replace("<projectile>", finalEntity.getName());

        if (finalBlock != null) message = message
                .replace("<block>", finalBlock.name());

        if (killer != null && finalEntity != null && !killer.getType().equals(finalEntity.getType())) {
            String dueToMessage = integrations.getString("DiscordSRV.message.death.due-to");
            message = message.replace("<due_to>", dueToMessage.replace("<killer>", killer.getName()));
        }

        if (killerItem != null) {
            String byItemMessage = integrations.getString("DiscordSRV.message.death.by-item");

            String itemName = killerItem.getItemMeta() != null && !killerItem.getItemMeta().getDisplayName().isEmpty()
                    ? net.md_5.bungee.api.ChatColor.ITALIC + killerItem.getItemMeta().getDisplayName()
                    : killerItem.getType().name();

            message = message.replace("<by_item>", byItemMessage.replace("<item>", itemName));
        }

        message = message.replace("<killer>", "")
                .replace("<projectile>", "")
                .replace("<block>", "")
                .replace("<due_to>", "")
                .replace("<by_item>", "");

        Map<String, String> replacements = new HashMap<>();

        replacements.put("<type>", message);
        replacements.put("<player>", sender.getName());

        sendMessage(sender, "death", replacements);
    }

    public void sendJoinMessage(@NotNull Player sender, @NotNull String type) {
        String message = integrations.getString("DiscordSRV.message.join.type." + type)
                .replace("<player>", sender.getName());

        Map<String, String> replacements = new HashMap<>();

        replacements.put("<type>", message);
        replacements.put("<player>", sender.getName());

        sendMessage(sender, "join", replacements);
    }

    public void sendQuitMessage(@NotNull Player sender, @NotNull String type) {
        String message = integrations.getString("DiscordSRV.message.quit.type." + type)
                .replace("<player>", sender.getName());

        Map<String, String> replacements = new HashMap<>();

        replacements.put("<type>", message);
        replacements.put("<player>", sender.getName());

        sendMessage(sender, "quit", replacements);
    }

    public void sendAdvancementMessage(@NotNull Player sender, @NotNull FAdvancement advancement) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", sender.getName());
        replacements.put("<advancement>", advancement.getTitle());

        sendMessage(sender, "advancement-" + advancement.getType(), replacements);
    }

    public void sendStreamMessage(@Nullable Player sender, @NotNull String urls) {
        String senderName = sender == null ? "CONSOLE" : sender.getName();

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", senderName);
        replacements.put("<urls>", urls);

        sendMessage(sender, "stream", replacements);
    }

    public void sendBanMessage(@Nullable OfflinePlayer sender, @NotNull Moderation moderation) {
        String type = moderation.getTime() == -1 ? "permanent" : "usually";
        String message = integrations.getString("DiscordSRV.message.ban.type." + type)
                .replace("<player>", moderation.getPlayerName())
                .replace("<time>", TimeUtil.convertTime(null, moderation.getRemainingTime()))
                .replace("<reason>", moderation.getReason())
                .replace("<moderator>", moderation.getModeratorName());

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", moderation.getModeratorName());
        replacements.put("<type>", message);

        sendMessage(sender, "ban", replacements);
    }

    public void sendMuteMessage(@Nullable OfflinePlayer sender, @NotNull Moderation moderation) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", moderation.getModeratorName());
        replacements.put("<time>", TimeUtil.convertTime(null, moderation.getRemainingTime()));
        replacements.put("<reason>", moderation.getReason());
        replacements.put("<moderator>", moderation.getModeratorName());
        sendMessage(sender, "mute", replacements);
    }

    public void sendWarnMessage(@Nullable OfflinePlayer sender, @NotNull Moderation moderation, int count) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", moderation.getModeratorName());
        replacements.put("<count>", String.valueOf(count));
        replacements.put("<time>", TimeUtil.convertTime(null, moderation.getRemainingTime()));
        replacements.put("<reason>", moderation.getReason());
        replacements.put("<moderator>", moderation.getModeratorName());
        sendMessage(sender, "mute", replacements);
    }

    public void sendKickMessage(@Nullable OfflinePlayer sender, @NotNull String reason, @NotNull String moderatorName) {
        String senderName = sender == null ? "CONSOLE" : sender.getName();

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", senderName);
        replacements.put("<reason>", reason);
        replacements.put("<moderator>", moderatorName);
        sendMessage(sender, "kick", replacements);
    }

    public void sendBroadcastMessage(@Nullable OfflinePlayer sender, @NotNull String message) {
        String senderName = sender == null ? "CONSOLE" : sender.getName();

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", senderName);
        replacements.put("<message>", message);
        sendMessage(sender, "broadcast", replacements);
    }

    public void sendMaintenanceMessage(@Nullable OfflinePlayer sender, @NotNull String type) {
        String senderName = sender == null ? "CONSOLE" : sender.getName();

        String message = integrations.getString("DiscordSRV.message.maintenance.type." + type)
                .replace("<player>", senderName);

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", senderName);
        replacements.put("<type>", message);
        sendMessage(sender, "maintenance", replacements);
    }

    public void sendPollMessage(@Nullable OfflinePlayer sender, @NotNull String message, int id) {
        String senderName = sender == null ? "CONSOLE" : sender.getName();

        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("<player>", senderName);
        replacements.put("<message>", message);
        replacements.put("<id>", String.valueOf(id));
        sendMessage(sender, "poll", replacements);
    }

    public void sendMessage(@Nullable OfflinePlayer sender, @NotNull String typeMessage, Map<String, String> replacements) {
        String path = "DiscordSRV.message." + typeMessage;

        String channelId = integrations.getString(path + ".channel-id");

        TextChannel textChannel = DiscordSRV.getPlugin().getJda().getTextChannelById(channelId);

        var messageBuilder = new github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder();

        if (integrations.getBoolean(path + ".embed.enable")) {

            EmbedBuilder embedBuilder = new EmbedBuilder();

            String color = integrations.getString(path + ".embed.color");
            embedBuilder.setColor(Color.decode(color.toUpperCase()));

            if (integrations.getBoolean(path + ".embed.image.enable")) {
                String imageUrl = integrations.getString(path + ".embed.image.url");
                embedBuilder.setImage(imageUrl);
            }

            if (integrations.getBoolean(path + ".embed.title.enable")) {
                String titleMessage = integrations.getString(path + ".embed.title.text");
                titleMessage = replace(titleMessage, replacements);

                embedBuilder.setTitle(titleMessage);
                if (integrations.getBoolean(path + ".embed.title.icon.enable")) {
                    String titleIconUrl = integrations.getString(path + ".embed.icon.url");
                    embedBuilder.setTitle(titleMessage, titleIconUrl);
                }
            }

            if (integrations.getBoolean(path + ".embed.description.enable")) {
                String description = integrations.getString(path + ".embed.description.text");
                description = replace(description, replacements);

                embedBuilder.setDescription(description);
            }

            if (integrations.getBoolean(path + ".embed.author.enable")) {
                String author = integrations.getString(path + ".embed.author.text");
                author = replace(author, replacements);

                embedBuilder.setAuthor(author);
                if (sender != null && integrations.getBoolean(path + ".embed.author.icon")) {
                    String avatarUrl = DiscordSRV.getAvatarUrl(sender.getName(), sender.getUniqueId());
                    Bukkit.broadcastMessage(avatarUrl);
                    embedBuilder.setAuthor(author, null, avatarUrl);
                }
            }

            if (integrations.getBoolean(path + ".embed.footer.enable")) {
                String footerMessage = integrations.getString(path + ".embed.footer.text");
                footerMessage = replace(footerMessage, replacements);

                embedBuilder.setFooter(footerMessage);
                if (integrations.getBoolean(path + ".embed.footer.icon.enable")) {
                    String iconUrl = integrations.getString(path + ".embed.footer.icon.url");
                    embedBuilder.setFooter(footerMessage, iconUrl);
                }
            }

            messageBuilder.setEmbeds(embedBuilder.build());
        }

        if (integrations.getBoolean(path + ".content.enable")) {
            String content = integrations.getString(path + ".content.text");
            content = replace(content, replacements);

            messageBuilder.setContent(content);
        }

        DiscordUtil.queueMessage(textChannel, messageBuilder.build(), true);
    }

    public String replace(@NotNull String string, @NotNull Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }

        return string;
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onChatMessageFromInGame(@NotNull GameChatMessagePreProcessEvent event) {
        Player player = event.getPlayer();

        String message = event.getMessage();

        List<String> features = integrations.getStringList("DiscordSRV.features");

        MessageBuilder messageBuilder = new MessageBuilder(player, player.getInventory().getItemInMainHand(), message, features);
        message = messageBuilder.getMessage("");

        event.setMessage(message);
    }

    @Override
    public void init() {
        DiscordSRV.api.subscribe(this);
        FlectoneChat.info("DiscordSRV detected and hooked");
    }

    public void unsubscribe() {
        DiscordSRV.api.unsubscribe(this);
    }
}
