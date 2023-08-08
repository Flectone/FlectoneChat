package net.flectone.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.flectone.Main;
import net.flectone.commands.CommandChatcolor;
import net.flectone.misc.entity.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.messages.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectUtil {

    @NotNull
    public static String convertTimeToString(int timeInSeconds) {
        if (timeInSeconds < 0) return "";

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

    @NotNull
    public static String convertTimeToString(long time) {
        String timeoutSecondsString = String.valueOf((time - System.currentTimeMillis()) / 1000).substring(1);
        int timeoutSeconds = Integer.parseInt(timeoutSecondsString);
        return convertTimeToString(timeoutSeconds);
    }

    @NotNull
    public static String toString(@NotNull String[] strings) {
        return toString(strings, 0);
    }

    @NotNull
    public static String toString(@NotNull String[] strings, int start) {
        return toString(strings, start, " ");
    }

    public static String toString(@NotNull String[] strings, int start, @NotNull String delimiter) {
        return Arrays.stream(strings, start, strings.length)
                .collect(Collectors.joining(delimiter));
    }

    @NotNull
    public static String translateHexToColor(@NotNull String string) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(string);
        StringBuilder sb = new StringBuilder();

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

    @NotNull
    public static String formatString(@NotNull String string, @Nullable CommandSender colorSender, @Nullable CommandSender papiSender) {
        return formatString(false, string, colorSender, papiSender);
    }

    @NotNull
    public static String formatString(boolean neededPermission, @NotNull String string, @Nullable CommandSender colorSender, @Nullable CommandSender papiSender) {
        String[] defaultColors = CommandChatcolor.getDefaultColors();

        if (colorSender instanceof Player player && FPlayerManager.getPlayer(player) != null) {

            if (Main.isHavePAPI && papiSender instanceof Player) {
                if (!neededPermission || papiSender.isOp() || papiSender.hasPermission("flectonechat.placeholders")){
                    string = PlaceholderAPI.setPlaceholders((Player) papiSender, string);
                    string = PlaceholderAPI.setRelationalPlaceholders((Player) papiSender, player, string);
                }
            }

            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) {
                return translateHexToColor(string
                        .replace("&&1", defaultColors[0])
                        .replace("&&2", defaultColors[1]));
            }

            return translateHexToColor(string
                    .replace("&&1", fPlayer.getColors()[0])
                    .replace("&&2", fPlayer.getColors()[1]));
        }

        return translateHexToColor(string
                .replace("&&1", defaultColors[0])
                .replace("&&2", defaultColors[1]));
    }

    @NotNull
    public static String formatString(@NotNull String string, @Nullable CommandSender colorSender) {
        return formatString(string, colorSender, colorSender);
    }

    public static int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static void playSound(@Nullable Player player, @NotNull String command) {
        if (player == null || !Main.config.getBoolean("sound." + command + ".enable")) return;

        String soundName = Main.config.getString("sound." + command + ".type");

        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1, 1);
        } catch (IllegalArgumentException exception) {
            Main.getInstance().getLogger().warning("Incorrect sound " + soundName + " for " + command + ".sound.type");
        }

    }

    @NotNull
    public static String buildFormattedMessage(@NotNull Player player, @NotNull String command, @Nullable String text, @NotNull ItemStack itemStack) {
        if (text == null) return "";

        MessageBuilder messageBuilder = new MessageBuilder(command, text, itemStack, false);
        String message = messageBuilder.getMessage();

        if (player.isOp() || player.hasPermission("flectonechat.formatting")) {
            message = ObjectUtil.formatString(message, player);
        }

        return message;
    }

    @NotNull
    public static ArrayList<String> splitLine(@NotNull String line, @NotNull ArrayList<String> placeholders) {
        ArrayList<String> split = new ArrayList<>(List.of(line));

        for (String placeholder : placeholders) {
            split = (ArrayList<String>) split.stream().flatMap(part -> {
                String[] sp = part.split("((?=" + placeholder + ")|(?<=" + placeholder + "))");
                return Arrays.stream(sp);
            }).collect(Collectors.toList());
        }

        return split;
    }
}
