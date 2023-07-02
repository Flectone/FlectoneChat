package net.flectone.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.flectone.custom.FCommands;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.flectone.custom.FPlayer;
import net.flectone.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isHavePAPI = false;

    public static TextComponent getNameComponent(String text, String playerName, CommandSender colorPlayer, CommandSender papiPlayer){
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("chat.click_player_name", colorPlayer, papiPlayer);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(text));

        if(papiPlayer instanceof ConsoleCommandSender) return textComponent;

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
    }

    public static void buildMessage(String message, ComponentBuilder componentBuilder, String chatColor, CommandSender colorPlayer, CommandSender papiPlayer, ItemStack itemStack) {
        // Ping player in message
        String pingPrefix = Main.config.getString("chat.ping.prefix");
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        BaseComponent[] colorComponent = TextComponent.fromLegacyText(chatColor);

        for(String word : message.split(" ")) {
            TextComponent wordComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + word));

            if(word.startsWith(pingPrefix) && FCommands.isRealOnlinePlayer(word.substring(1))){

                Player player = Bukkit.getPlayer(word.substring(1));

                String pingMessage = Main.locale.getFormatString("chat.ping.message", colorPlayer, papiPlayer)
                        .replace("<player>", player.getName())
                        .replace("<prefix>", pingPrefix);

                wordComponent = getNameComponent(pingMessage, player.getName(), colorPlayer, player);
            }

            Matcher urlMatcher = pattern.matcher(word);
            if (urlMatcher.find()) {
                wordComponent = new TextComponent(TextComponent.fromLegacyText(Main.config.getFormatString("chat.color.url", colorPlayer, papiPlayer) + word));
                wordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, word.substring(urlMatcher.start(0), urlMatcher.end(0))));
                wordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Main.locale.getFormatString("chat.click_url", colorPlayer, papiPlayer))));
            }

            if (itemStack != null && word.contains("%item%")) {
                String[] words;
                TranslatableComponent item = null;

                words = word.split("%item%");
                if (words.length < 2) {
                    words = new String[]{words.length > 0 ? words[0] : "", ""};
                }

                String[] formattedItemArray = ReflectionUtils.getFormattedStringItem(itemStack);
                item = new TranslatableComponent(formattedItemArray[0]);
                item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(formattedItemArray[1])}));

                String[] componentsStrings = Main.config.getFormatString("chat.color.tooltip", colorPlayer, papiPlayer).split("<tooltip>");

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

    public static String translateColor(String string, CommandSender colorSender, CommandSender papiSender){
        if(colorSender instanceof Player){
            Player player = ((Player) colorSender).getPlayer();

            if(Utils.isHavePAPI && string != null && papiSender instanceof Player) string = PlaceholderAPI.setPlaceholders((Player) papiSender, string);

            FPlayer fPlayer = PlayerUtils.getPlayer(player);
            return Utils.translateColor(string
                    .replace("&&1", fPlayer.getColors().get(0))
                    .replace("&&2", fPlayer.getColors().get(1)));
        }

        return Utils.translateColor(string
                .replace("&&1", Main.config.getString("color.first"))
                .replace("&&2", Main.config.getString("color.second")));
    }

    public static String translateColor(String string, CommandSender colorSender){
        return translateColor(string, colorSender, colorSender);
    }

    public static int getCurrentTime(){
        return (int) (System.currentTimeMillis()/1000);
    }
}
