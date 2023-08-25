package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandReply implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.getFPlayer() == null) return true;

        if (fCommand.getFPlayer().getLastWriter() == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return true;
        }

        FPlayer secondFPlayer = FPlayerManager.getPlayer(fCommand.getFPlayer().getLastWriter());

        if (secondFPlayer == null || secondFPlayer.getPlayer() == null || !secondFPlayer.isOnline()) {
            fCommand.sendMeMessage("command.reply.no-receiver");
            return true;
        }

        if (!secondFPlayer.getChatInfo().getOption("reply")) {
            fCommand.sendMeMessage("command.he-disabled");
            return true;
        }

        if (fCommand.getFPlayer().isIgnored(secondFPlayer.getPlayer())) {
            fCommand.sendMeMessage("command.you_ignore");
            return true;
        }

        if (secondFPlayer.isIgnored(fCommand.getPlayer())) {
            fCommand.sendMeMessage("command.he_ignore");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (fCommand.isMuted()) return true;

        String message = ObjectUtil.toString(strings);
        fCommand.sendTellMessage(commandSender, secondFPlayer.getPlayer(), message);

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
        return "reply";
    }
}
