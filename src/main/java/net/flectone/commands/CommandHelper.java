package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.locale;
import static net.flectone.managers.FileManager.config;

public class CommandHelper implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()
                || fCommand.isInsufficientArgs(1)) return true;

        String permission = config.getString("command.helper.see.permission");

        Set<Player> playerSet = Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(player -> player.hasPermission(permission))
                .collect(Collectors.toSet());

        if (playerSet.isEmpty()) {
            fCommand.sendMeMessage("command.helper.no-helpers");
            return true;
        }

        String formatMessage = locale.getString("command.helper.global-message")
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendFilterGlobalMessage(playerSet, formatMessage, ObjectUtil.toString(strings, 0), null, true);

        fCommand.sendMeMessage("command.helper.local-message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isTabCompleteMessage(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "helper";
    }
}
