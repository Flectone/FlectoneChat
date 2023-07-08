package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandOnline extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if(fPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if(command.getName().equalsIgnoreCase("lastonline") && fPlayer.isOnline()){
            fCommand.sendMeMessage("command.online.last.currently-message", "<player>", fPlayer.getRealName());
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        long playedTime = command.getName().equalsIgnoreCase("lastonline") ? fPlayer.getOfflinePlayer().getLastPlayed() : fPlayer.getOfflinePlayer().getFirstPlayed();

        String[] replacedStrings = {"<player>", "<time>"};
        String[] replacedToStrings = {fPlayer.getRealName(), ObjectUtil.convertTimeToString(playedTime)};

        fCommand.sendMeMessage("command.online." + command.getName().split("online")[0] + ".message", replacedStrings, replacedToStrings);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOfflinePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
