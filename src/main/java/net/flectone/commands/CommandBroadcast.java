package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class CommandBroadcast implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)
                || fCommand.isHaveCD()
                || fCommand.isMuted()) return true;

        String formatString = locale.getString("command.broadcast.message")
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendGlobalMessage(formatString, ObjectUtil.toString(strings), null, false);

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
        return "broadcast";
    }
}
