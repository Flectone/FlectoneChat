package net.flectone.misc.commands;

import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface FTabCompleter extends CommandExecutor, TabCompleter {

    List<String> wordsList = Collections.synchronizedList(new ArrayList<>());

    String getCommandName();

    default void isStartsWith(@NotNull String arg, @NotNull String string) {
        if (string.toLowerCase().startsWith(arg.toLowerCase()) || arg.replace(" ", "").isEmpty()) {
            if (wordsList.contains(string)) return;
            wordsList.add(string);
        }
    }

    default void addKeysFile(@NotNull FileManager fileManager, @NotNull String arg) {
        fileManager.getKeys().parallelStream()
                .filter(key -> !fileManager.getString(key).contains("root='YamlConfiguration'"))
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
        Arrays.stream(FCommand.formatTimeList).parallel()
                .forEach(format -> {
                    if (arg.length() != 0 && StringUtils.isNumeric(arg.substring(arg.length() - 1))) {
                        isStartsWith(arg, arg + format);
                    } else {
                        for (int x = 1; x < 10; x++) {
                            isStartsWith(arg, x + format);
                        }
                    }
                });
    }
}
