package net.flectone.commands;

import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandIgnore extends FTabCompleter {

    public CommandIgnore() {
        super.commandName = "ignore";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        if (fCommand.isInsufficientArgs(1)) return true;

        if (fCommand.isSelfCommand()) {
            fCommand.sendMeMessage("command.ignore.myself");
            return true;
        }

        String playerName = strings[0];
        FPlayer ignoredFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (ignoredFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        ArrayList<String> ignoreList = fCommand.getFPlayer().getIgnoreList();

        boolean isIgnored = fCommand.getFPlayer().isIgnored(ignoredFPlayer.getUUID());
        fCommand.sendMeMessage("command.ignore." + !isIgnored + "-message", "<player>", ignoredFPlayer.getRealName());

        if (isIgnored) ignoreList.remove(ignoredFPlayer.getUUID());
        else ignoreList.add(ignoredFPlayer.getUUID());

        fCommand.getFPlayer().setIgnoreList(ignoreList);
        fCommand.getFPlayer().setUpdated(true);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isOfflinePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
