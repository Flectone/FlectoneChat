package net.flectone.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.managers.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectUtil {

    public static String convertTimeToString(int timeInSeconds){
        int days = (timeInSeconds / 86400);
        int hours = (timeInSeconds / 3600) % 24;
        int minutes = (timeInSeconds / 60) % 60;
        int seconds = timeInSeconds % 60;

        String finalString = (days > 0 ? " " + days + Main.locale.getString("online.format.day") : "")
                + (hours > 0 ? " " + hours + Main.locale.getString("online.format.hour") : "")
                + (minutes > 0 ? " " + minutes + Main.locale.getString("online.format.minute") : "")
                + (seconds > 0 ? " " + seconds + Main.locale.getString("online.format.second") : "");

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
        return Arrays.stream(strings, start, strings.length)
                .collect(Collectors.joining(" "));
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
        if(colorSender instanceof Player){
            Player player = ((Player) colorSender).getPlayer();

            if(Main.isHavePAPI && string != null && papiSender instanceof Player) string = PlaceholderAPI.setPlaceholders((Player) papiSender, string);

            FPlayer fPlayer = PlayerManager.getPlayer(player);

            return translateHexToColor(string
                    .replace("&&1", fPlayer.getColors().get(0))
                    .replace("&&2", fPlayer.getColors().get(1)));
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
        if(player == null || !Main.config.getBoolean(command + ".sound.enable")) return;

        String soundName = Main.config.getString(command + ".sound.type");

        try {
            player.playSound(player, Sound.valueOf(soundName), 1, 1);
        } catch (IllegalArgumentException exception){
            Main.getInstance().getLogger().warning("Incorrect sound " + soundName + " for " + command + ".sound.type");
        }

    }
}
