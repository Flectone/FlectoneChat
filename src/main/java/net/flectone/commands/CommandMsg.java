package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMsg extends FTabCompleter {

    public CommandMsg(){
        super.commandName = "msg";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];
        FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(playerName);
        if(secondFPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        String message = ObjectUtil.toString(strings, 1);

        if(!fCommand.isConsole()){
            if(fCommand.getSenderName().equalsIgnoreCase(playerName)){
                commandSender.sendMessage(Main.locale.getFormatString("command.msg.myself", commandSender) + message);
                return true;
            }

            if(fCommand.getFPlayer().isIgnored(secondFPlayer.getPlayer())){
                fCommand.sendMeMessage("command.you_ignore");
                return true;
            }
            if(secondFPlayer.isIgnored((Player) commandSender)){
                fCommand.sendMeMessage("command.he_ignore");
                return true;
            }
        }

        fCommand.sendTellMessage(commandSender, secondFPlayer.getPlayer(), "send", message);
        fCommand.sendTellMessage(secondFPlayer.getPlayer(), commandSender, "get", message);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOnlinePlayer(strings[0]);
        } else if(strings.length == 2) {
            isStartsWith(strings[1], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

}
