package net.flectone.misc.commands;

import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.misc.files.FYamlConfiguration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public interface FTabCompleter extends CommandExecutor, TabCompleter {

    List<String> wordsList = Collections.synchronizedList(new ArrayList<>());

    String getCommandName();

    default boolean isEnable() {
        return FileManager.config.getBoolean("command." + getCommandName() + ".enable");
    }

    default void isStartsWith(@NotNull String arg, @NotNull String string) {
        if (string.toLowerCase().startsWith(arg.toLowerCase()) || arg.replace(" ", "").isEmpty()) {
            if (wordsList.contains(string)) return;
            wordsList.add(string);
        }
    }

    default void isFileKey(@NotNull FYamlConfiguration file, @NotNull String arg) {
        file.getKeys(true).parallelStream()
                .filter(key -> !file.getString(key).contains("root='FYamlConfiguration'"))
                .forEachOrdered(key -> isStartsWith(arg, key));
    }

    default void isOfflinePlayer(@NotNull String arg) {
        FPlayerManager.getPlayers().parallelStream().filter(Objects::nonNull)
                .forEachOrdered(offlinePlayer -> isStartsWith(arg, offlinePlayer.getRealName()));
    }

    default void isOnlinePlayer(@NotNull String arg) {
        Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> !FSuperVanish.isVanished(player))
                .forEach(player -> isStartsWith(arg, player.getName()));
    }

    default void isFormatString(@NotNull String arg) {
        Arrays.stream(FCommand.formatTimeList)
                .forEach(format -> {
                    if (arg.length() != 0 && StringUtils.isNumeric(arg.substring(arg.length() - 1))) {
                        isStartsWith(arg, arg + format);
                        return;
                    }

                    isDigitInArray(arg, format, 1, 10);

                });
    }

    default void isTabCompleteMessage(@NotNull String arg) {
        isTabCompleteMessage(arg, "tab-complete.message");
    }

    default void isTabCompleteMessage(String arg, String localeKey) {
        isStartsWith(arg, FileManager.locale.getString(localeKey));
    }

    default void isDigitInArray(String arg, String string, int start, int end) {
        for (int x = start; x < end; x++) {
            isStartsWith(arg, x + string);
        }
    }
}
