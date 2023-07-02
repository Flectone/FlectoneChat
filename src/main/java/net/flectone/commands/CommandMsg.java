package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.managers.PlayerManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMsg extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];

        Player secondPlayer = Bukkit.getPlayer(playerName);
        if(secondPlayer == null){
            fCommand.sendMeMessage("msg.no_player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        String message = ObjectUtil.toString(strings, 1);

        if(!fCommand.isConsole()){
            if(fCommand.getSenderName().equalsIgnoreCase(playerName)){
                commandSender.sendMessage(Main.locale.getFormatString("msg.myself", commandSender) + message);
                return true;
            }

            if(fCommand.getFPlayer().checkIgnoreList(secondPlayer)){
                fCommand.sendMeMessage("msg.you_ignore");
                return true;
            }
            if(PlayerManager.getPlayer(secondPlayer).checkIgnoreList((Player) commandSender)){
                fCommand.sendMeMessage("msg.he_ignore");
                return true;
            }
        }

        fCommand.sendTellMessage(commandSender, secondPlayer.getPlayer(), "send", message);
        fCommand.sendTellMessage(secondPlayer.getPlayer(), commandSender, "get", message);

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
