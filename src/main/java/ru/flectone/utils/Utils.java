package ru.flectone.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.flectone.FPlayer;
import ru.flectone.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isHavePAPI = false;

    public static TextComponent getNameComponent(String text, String playerName, Player player){
        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(text));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + playerName + " "));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.click_player_name", player))));
        return textComponent;
    }

    public static void buildMessage(String message, ComponentBuilder componentBuilder, String chatColor, Player player){
        buildMessage(message, componentBuilder, chatColor, player, null);
    }

    public static void buildMessage(String message, ComponentBuilder componentBuilder, String chatColor, Player player, ItemStack itemStack) {
        // Ping player in message
        String pingPrefix = Main.config.getString("chat.ping.prefix");
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        BaseComponent[] colorComponent = TextComponent.fromLegacyText(chatColor);

        for(String word : message.split(" ")) {
            TextComponent wordComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + word));

            for(Player playerOnline : Bukkit.getOnlinePlayers()){
                if (!word.equalsIgnoreCase(pingPrefix + playerOnline.getName())) {
                    continue;
                }

                String pingMessage = Main.locale.getFormatString("chat.ping.message", player)
                        .replace("<player>", playerOnline.getName())
                        .replace("<prefix>", pingPrefix);

                wordComponent = getNameComponent(pingMessage, playerOnline.getName(), player);
            }

            Matcher urlMatcher = pattern.matcher(word);
            if (urlMatcher.find()) {
                wordComponent = new TextComponent(TextComponent.fromLegacyText(Main.config.getFormatString("chat.color.url", player) + word));
                wordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, word.substring(urlMatcher.start(0), urlMatcher.end(0))));
                wordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.click_url", player))));
            }

            if (itemStack != null && word.contains("%item%")) {
                String[] words;
                TranslatableComponent item = null;

                words = word.split("%item%");
                if (words.length < 2) {
                    words = new String[]{words.length > 0 ? words[0] : "", ""};
                }

                String[] formattedItemArray = ReflectionUtil.getFormattedStringItem(itemStack);
                item = new TranslatableComponent(formattedItemArray[0]);
                item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(formattedItemArray[1])}));

                String[] componentsStrings = Main.config.getFormatString("chat.color.tooltip", player).split("<tooltip>");

                componentBuilder
                        .append(colorComponent)
                        .append(words[0])
                        .append(TextComponent.fromLegacyText(componentsStrings[0]))
                        .append(item)
                        .append(TextComponent.fromLegacyText(componentsStrings[1]))
                        .append(colorComponent)
                        .append(words[1])
                        .append(" ");

                continue;
            }

            componentBuilder.append(wordComponent, ComponentBuilder.FormatRetention.NONE).append(" ");
        }
    }


    public static String translateColor(String string) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(string);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group();
            String replaceSharp = hexCode.replace('#', 'x');

            StringBuilder builder = new StringBuilder();
            for (char c : replaceSharp.toCharArray()) {
                builder.append("&").append(c);
            }
            matcher.appendReplacement(sb, builder.toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static String translateColor(String string, Player player){
        if(Utils.isHavePAPI && string != null) string = PlaceholderAPI.setPlaceholders(player, string);

        if (player != null) {
            FPlayer fPlayer = PlayerUtils.getPlayer(player);
            return Utils.translateColor(string
                    .replace("&&1", fPlayer.getColors().get(0))
                    .replace("&&2", fPlayer.getColors().get(1)));
        } else {
            return Utils.translateColor(string
                    .replace("&&1", Main.getInstance().config.getString("color.first"))
                    .replace("&&2", Main.getInstance().config.getString("color.second")));
        }
    }
}
