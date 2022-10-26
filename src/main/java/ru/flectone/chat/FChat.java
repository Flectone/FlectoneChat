package ru.flectone.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.units.qual.C;
import ru.flectone.FPlayer;
import ru.flectone.Main;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.awt.*;
import java.util.Set;

public class FChat implements Listener {


    private FileResource locale = Main.locale;

    private FileResource config = Main.config;

    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        Set<Player> recipients = event.getRecipients();

        //If recipient ignored event player
        recipients.removeIf(recipient -> PlayerUtils.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString()));

        String chatType = "global";
        String message = event.getMessage();
        String globalPrefix = config.getString("chat.global.prefix");

        //If local chat
        if(!message.startsWith(globalPrefix) || message.equals(globalPrefix) || !config.getBoolean("chat.global.enable")){

            chatType = "locale";

            int localeRange = config.getInt("chat.locale.range");

            recipients.removeIf(recipient -> player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localeRange);

            if(recipients.size() == 1 && config.getBoolean("chat.no_recipients.enable")){
                player.sendMessage(locale.getFormatString("chat.no_recipients", player));
            }

        } else {

            message = message
                    .replaceFirst(globalPrefix + " ", "")
                    .replaceFirst(globalPrefix, "");
        }

        createMessage(recipients, player, message, chatType);

        event.getRecipients().clear();

    }

    public void createMessage(Set<Player> recipients, Player player, String message, String chatType){

        for(Player recipient : recipients){

            ComponentBuilder finalBuilder = new ComponentBuilder();

            String[] localeMessage = config.getFormatString("chat." + chatType + ".message", recipient).split("<player>");

            TextComponent playerNickName = new TextComponent(TextComponent.fromLegacyText(localeMessage[0] + PlayerUtils.getPlayer(player).getName()));
            playerNickName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName() + " "));
            playerNickName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(locale.getFormatString("chat.click_player_name", recipient))));

            finalBuilder.append(playerNickName);

            finalBuilder.append(TextComponent.fromLegacyText(localeMessage[1]), ComponentBuilder.FormatRetention.NONE);

            Utils.buildMessage(message, finalBuilder, org.bukkit.ChatColor.getLastColors(localeMessage[1]), recipient);


            recipient.spigot().sendMessage(finalBuilder.create());
        }

    }

}
