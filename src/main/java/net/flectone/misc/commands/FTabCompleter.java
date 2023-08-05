package net.flectone.misc.commands;

import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FTabCompleter implements CommandExecutor, TabCompleter {

    protected String commandName;
    protected List<String> wordsList = Collections.synchronizedList(new ArrayList<>());

    public String getCommandName() {
        return commandName;
    }

    protected void isStartsWith(String arg, String string) {
        if (string.toLowerCase().startsWith(arg.toLowerCase()) || arg.replace(" ", "").isEmpty()) {
            if(wordsList.contains(string)) return;
            wordsList.add(string);
        }
    }

    protected void addKeysFile(FileManager fileManager, String arg) {
        fileManager.getKeys().parallelStream()
                .filter(key -> !fileManager.getString(key).contains("root='YamlConfiguration'"))
                .forEachOrdered(key -> isStartsWith(arg, key));
    }

    protected void isOfflinePlayer(String arg) {
        FPlayerManager.getPlayers().parallelStream().filter(Objects::nonNull)
                .forEachOrdered(offlinePlayer -> isStartsWith(arg, offlinePlayer.getRealName()));
    }

    protected void isOnlinePlayer(String arg) {
        Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> !FSuperVanish.isVanished(player))
                .forEach(player -> isStartsWith(arg, player.getName()));
    }

    protected void isFormatString(String arg) {
        Arrays.stream(FCommands.formatTimeList).parallel()
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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
