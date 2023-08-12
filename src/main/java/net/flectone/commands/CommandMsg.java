package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class CommandMsg implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];
        FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(playerName);
        if (secondFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        String message = ObjectUtil.toString(strings, 1);

        if (!secondFPlayer.isOnline()) {
            Bukkit.dispatchCommand(commandSender, "mail " + playerName + " " + message);
            return true;
        }

        if (!fCommand.isConsole()) {
            if (fCommand.getSenderName().equalsIgnoreCase(playerName)) {
                commandSender.sendMessage(locale.getFormatString("command.msg.myself", commandSender) + message);
                return true;
            }

            if (fCommand.getFPlayer() != null && fCommand.getFPlayer().isIgnored(secondFPlayer.getPlayer())) {
                fCommand.sendMeMessage("command.you_ignore");
                return true;
            }

            if (secondFPlayer.isIgnored((Player) commandSender)) {
                fCommand.sendMeMessage("command.he_ignore");
                return true;
            }
        }

        if(secondFPlayer.getPlayer() == null) return true;

        fCommand.sendTellMessage(commandSender, secondFPlayer.getPlayer(), message);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isOfflinePlayer(strings[0]);
            case 2 -> isStartsWith(strings[1], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "msg";
    }
}
