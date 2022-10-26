package ru.flectone.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.flectone.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static TextComponent getNameComponent(String text, String playerName, Player player){
        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(text));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.click_player_name", player))));
        return textComponent;
    }

    public static void buildMessage(String message, ComponentBuilder componentBuilder, String chatColor, Player player){
        //Ping player in message
        String pingPrefix = Main.config.getString("chat.ping.prefix");
        for(String word : message.split(" ")){
            TextComponent wordComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + word));
            for(Player playerOnline : Bukkit.getOnlinePlayers()){
                if(!word.equalsIgnoreCase(pingPrefix + playerOnline.getName())) continue;

                String pingMessage = Main.locale.getFormatString("chat.ping.message", player)
                        .replace("<player>", playerOnline.getName())
                        .replace("<prefix>", pingPrefix);

                wordComponent = getNameComponent(pingMessage, playerOnline.getName(), player);
            }
            componentBuilder.append(wordComponent, ComponentBuilder.FormatRetention.NONE).append(" ");
        }
    }

    public static String translateColor(String string){
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String hexCode = string.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&" + c);
            }
            string = string.replace(hexCode, builder.toString());
            matcher = pattern.matcher(string);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
