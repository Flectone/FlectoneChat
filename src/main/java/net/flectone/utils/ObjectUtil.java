package net.flectone.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.messages.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectUtil {

    public static String convertTimeToString(int timeInSeconds){
        if(timeInSeconds < 0) return "";

        int days = (timeInSeconds / 86400);
        int hours = (timeInSeconds / 3600) % 24;
        int minutes = (timeInSeconds / 60) % 60;
        int seconds = timeInSeconds % 60;

        String finalString = (days > 0 ? " " + days + Main.locale.getString("command.online.format.day") : "")
                + (hours > 0 ? " " + hours + Main.locale.getString("command.online.format.hour") : "")
                + (minutes > 0 ? " " + minutes + Main.locale.getString("command.online.format.minute") : "")
                + (seconds > 0 ? " " + seconds + Main.locale.getString("command.online.format.second") : "");

        return finalString.substring(1);
    }

    public static String convertTimeToString(long time){
        String timeoutSecondsString = String.valueOf((time - System.currentTimeMillis()) / 1000).substring(1);
        int timeoutSeconds = Integer.parseInt(timeoutSecondsString);
        return convertTimeToString(timeoutSeconds);
    }

    public static String toString(String[] strings){
        return toString(strings, 0);
    }


    public static String toString(String[] strings, int start){
        return toString(strings, start, " ");
    }

    public static String toString(String[] strings, int start, String delimiter){
        return Arrays.stream(strings, start, strings.length)
                .collect(Collectors.joining(delimiter));
    }

    public static String translateHexToColor(String string) {
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

    public static String formatString(String string, CommandSender colorSender, CommandSender papiSender){
        return formatString(false, string, colorSender, papiSender);
    }

    public static String formatString(boolean neededPermission, String string, CommandSender colorSender, CommandSender papiSender){
        if(colorSender instanceof Player){
            Player player = ((Player) colorSender).getPlayer();

            if(Main.isHavePAPI && string != null && papiSender instanceof Player) {
                if(!neededPermission || papiSender.isOp() || papiSender.hasPermission("flectonechat.placeholders"))
                    string = PlaceholderAPI.setPlaceholders((Player) papiSender, string);

            }

            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            return translateHexToColor(string
                    .replace("&&1", fPlayer.getColors()[0])
                    .replace("&&2", fPlayer.getColors()[1]));
        }

        return translateHexToColor(string
                .replace("&&1", Main.config.getString("color.first"))
                .replace("&&2", Main.config.getString("color.second")));
    }

    public static String formatString(String string, CommandSender colorSender){
        return formatString(string, colorSender, colorSender);
    }

    public static int getCurrentTime(){
        return (int) (System.currentTimeMillis()/1000);
    }

    public static void playSound(Player player, String command){
        if(player == null || !Main.config.getBoolean("sound." + command + ".enable")) return;

        String soundName = Main.config.getString("sound." + command + ".type");

        try {
            player.playSound(player, Sound.valueOf(soundName), 1, 1);
        } catch (IllegalArgumentException exception){
            Main.getInstance().getLogger().warning("Incorrect sound " + soundName + " for " + command + ".sound.type");
        }

    }

    public static String buildFormattedMessage(Player player, String command, String text, ItemStack itemStack){
        if(text == null) return "";

        MessageBuilder messageBuilder = new MessageBuilder(command, text, itemStack, false);
        String message = messageBuilder.getMessage();

        if(player.isOp() || player.hasPermission("flectonechat.formatting")){
            message = ObjectUtil.formatString(message, player);
        }

        return message;
    }
}
