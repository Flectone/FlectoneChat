package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
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

public class CommandHelper extends FTabCompleter {

    public CommandHelper() {
        super.commandName = "helper";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        if (fCommand.isInsufficientArgs(1)) return true;

        String permission = Main.config.getString("command.helper.see.permission");

        Set<Player> playerSet = Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(player -> player.isOp() || player.hasPermission(permission))
                .collect(Collectors.toSet());

        if (playerSet.size() == 0) {
            fCommand.sendMeMessage("command.helper.no-helpers");
            return true;
        }

        String formatMessage = Main.locale.getString("command.helper.global-message")
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendGlobalMessage(playerSet, formatMessage, ObjectUtil.toString(strings, 0), null, true);

        fCommand.sendMeMessage("command.helper.local-message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
