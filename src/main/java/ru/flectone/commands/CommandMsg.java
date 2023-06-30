package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.utils.ObjectUtils;
import ru.flectone.utils.PlayerUtils;

public class CommandMsg implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(2)) return true;

        String playerName = strings[0];

        Player secondPlayer = Bukkit.getPlayer(playerName);
        if(secondPlayer == null){
            fCommand.sendMeMessage("msg.no_player");
            return true;
        }

        String message = ObjectUtils.toString(strings, 1);

        if(!fCommand.isConsole()){
            if(fCommand.getSenderName().equalsIgnoreCase(playerName)){
                commandSender.sendMessage(Main.locale.getFormatString("msg.myself", commandSender) + message);
                return true;
            }

            if(fCommand.getFPlayer().checkIgnoreList(secondPlayer)){
                fCommand.sendMeMessage("msg.you_ignore");
                return true;
            }
            if(PlayerUtils.getPlayer(secondPlayer).checkIgnoreList((Player) commandSender)){
                fCommand.sendMeMessage("msg.he_ignore");
                return true;
            }
        }

        fCommand.usingTellUtils(commandSender, secondPlayer.getPlayer(), "send", message);
        fCommand.usingTellUtils(secondPlayer.getPlayer(), commandSender, "get", message);

        return true;
    }


}
