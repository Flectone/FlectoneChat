package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandIgnore implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()
                || fCommand.isInsufficientArgs(1)) return true;

        if (strings[0].equalsIgnoreCase(commandSender.getName())) {
            fCommand.sendMeMessage("command.ignore.myself");
            return true;
        }

        String playerName = strings[0];
        FPlayer ignoredFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (ignoredFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.getFPlayer() == null) return true;

        ArrayList<UUID> ignoreList = fCommand.getFPlayer().getIgnoreList();

        boolean isIgnored = fCommand.getFPlayer().isIgnored(ignoredFPlayer.getUUID());
        fCommand.sendMeMessage("command.ignore." + !isIgnored + "-message", "<player>", ignoredFPlayer.getRealName());

        if (isIgnored) ignoreList.remove(ignoredFPlayer.getUUID());
        else ignoreList.add(ignoredFPlayer.getUUID());

        fCommand.getFPlayer().setIgnoreList(ignoreList);
        Main.getDataThreadPool().execute(() ->
                Main.getDatabase().updateFPlayer(fCommand.getFPlayer(), "ignore_list"));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isConfigOnlineModePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "ignore";
    }
}
