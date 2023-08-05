package net.flectone.integrations.discordsrv;

import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.MessageFormat;
import github.scarsz.discordsrv.util.DiscordUtil;
import github.scarsz.discordsrv.util.MessageUtil;
import github.scarsz.discordsrv.util.PlaceholderUtil;
import github.scarsz.discordsrv.util.TimeUtil;
import net.flectone.Main;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.function.BiFunction;

public class FDiscordSRV implements Listener {

    private static boolean isEnable = false;

    public FDiscordSRV(){
        isEnable = true;
    }

    public static void register(){
        new FDiscordSRV();
    }

    public static void sendDeathMessage(Player player, String message, Entity finalEntity, Material finalBlock, Entity killer, ItemStack killerItem){
        if(!isEnable) return;

        message = message.replace("<player>", player.getName());
        if(finalEntity != null) message = message
                .replace("<killer>", finalEntity.getName())
                .replace("<projectile>", finalEntity.getName());

        if(finalBlock != null) message = message
                .replace("<block>", finalBlock.name());

        if(killer != null) {
            String dueToMessage = Main.locale.getFormatString("death.due-to", null);
            message = message.replace("<due_to>", dueToMessage.replace("<killer>", killer.getName()));
        }

        if(killerItem != null){
            String byItemMessage = Main.locale.getFormatString("death.by-item", null);

            String itemName = killerItem.getItemMeta() != null && !killerItem.getItemMeta().getDisplayName().isEmpty()
                    ? killerItem.getItemMeta().getDisplayName()
                    : killerItem.getType().name();

            message = message.replace("<by_item>", byItemMessage.replace("<item>", itemName));
        }

        message = ObjectUtil.formatString(message, null)
                .replace("<killer>", "")
                .replace("<projectile>", "")
                .replace("<block>", "")
                .replace("<due_to>", "")
                .replace("<by_item>", "");

        sendAvatarMessage(message, player, "deaths");
    }

    public static void sendModerationMessage(String message){
        if(!isEnable) return;

        message = ObjectUtil.formatString(message, null);
        message = PlaceholderUtil.replacePlaceholdersToDiscord(message);

        String channelName = DiscordSRV.getPlugin().getOptionalChannel("global");
        TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);

        Message messageFormat = new MessageBuilder()
                .setEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription(message).build())
                .build();

        DiscordUtil.queueMessage(textChannel, messageFormat, true);
    }

    private static void sendAvatarMessage(String deathMessage, Player player){
        sendAvatarMessage(deathMessage, player, "global");
    }

    private static void sendAvatarMessage(String deathMessage, Player player, String optionalChannel) {

        String channelName = DiscordSRV.getPlugin().getOptionalChannel(optionalChannel);
        MessageFormat messageFormat = DiscordSRV.getPlugin().getMessageFromConfiguration("MinecraftPlayerDeathMessage");
        if (messageFormat == null) return;

        String finalDeathMessage = StringUtils.isNotBlank(deathMessage) ? deathMessage : "";
        String avatarUrl = DiscordSRV.getAvatarUrl(player);
        String botAvatarUrl = DiscordUtil.getJda().getSelfUser().getEffectiveAvatarUrl();
        String botName = DiscordSRV.getPlugin().getMainGuild() != null ? DiscordSRV.getPlugin().getMainGuild().getSelfMember().getEffectiveName() : DiscordUtil.getJda().getSelfUser().getName();
        String displayName = StringUtils.isNotBlank(player.getDisplayName()) ? MessageUtil.strip(player.getDisplayName()) : "";

        TextChannel destinationChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
        BiFunction<String, Boolean, String> translator = (content, needsEscape) -> {
            if (content == null) return null;
            content = content
                    .replaceAll("%time%|%date%", TimeUtil.timeStamp())
                    .replace("%username%", needsEscape ? DiscordUtil.escapeMarkdown(player.getName()) : player.getName())
                    .replace("%displayname%", needsEscape ? DiscordUtil.escapeMarkdown(displayName) : displayName)
                    .replace("%usernamenoescapes%", player.getName())
                    .replace("%displaynamenoescapes%", displayName)
                    .replace("%world%", player.getWorld().getName())
                    .replace("%deathmessage%", MessageUtil.strip(needsEscape ? DiscordUtil.escapeMarkdown(finalDeathMessage) : finalDeathMessage))
                    .replace("%deathmessagenoescapes%", MessageUtil.strip(finalDeathMessage))
                    .replace("%embedavatarurl%", avatarUrl)
                    .replace("%botavatarurl%", botAvatarUrl)
                    .replace("%botname%", botName);
            if (destinationChannel != null) content = DiscordUtil.translateEmotes(content, destinationChannel.getGuild());
            content = PlaceholderUtil.replacePlaceholdersToDiscord(content, player);
            return content;
        };
        Message discordMessage = DiscordSRV.translateMessage(messageFormat, translator);
        if (discordMessage == null) return;

        if (DiscordSRV.getLength(discordMessage) < 3) {
            DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Not sending death message, because it's less than three characters long. Message: " + messageFormat);
            return;
        }

        TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
        DiscordUtil.queueMessage(textChannel, discordMessage, true);
    }
}
