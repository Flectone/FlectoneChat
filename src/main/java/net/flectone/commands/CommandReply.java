package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.utils.ObjectUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.utils.PlayerUtils;

public class CommandReply implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        Player secondPlayer = PlayerUtils.getPlayer(((Player) commandSender).getUniqueId()).getLastWritePlayer();

        if(secondPlayer == null){
            fCommand.sendMeMessage("reply.no_answer");
            return true;
        }

        if(!secondPlayer.isOnline()){
            fCommand.sendMeMessage("reply.no_online");
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

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        String message = ObjectUtils.toString(strings);
        fCommand.usingTellUtils(commandSender, secondPlayer, "send", message);
        fCommand.usingTellUtils(secondPlayer, commandSender, "get", message);

        return true;
    }
}
