package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.custom.FCommands;
import ru.flectone.utils.ObjectUtils;

public class CommandOnline implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(1)) return true;

        String playerName = strings[0];

        if(command.getName().equalsIgnoreCase("lastonline")){
            Player player = Bukkit.getPlayer(playerName);

            if(player != null){
                fCommand.sendMeMessage("lastonline.message_now", "<player>", player.getName());
                return true;
            }
        }

        if(!FCommands.isRealOfflinePlayer(playerName)){
            fCommand.sendMeMessage("online.no_player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        long playedTime = command.getName().equalsIgnoreCase("lastonline") ? offlinePlayer.getLastPlayed() : offlinePlayer.getFirstPlayed();

        String[] replacedStrings = {"<player>", "<time>"};
        String[] replacedToStrings = {offlinePlayer.getName(), ObjectUtils.convertTimeToString(playedTime)};

        fCommand.sendMeMessage(command.getName() + ".message", replacedStrings, replacedToStrings);

        return true;
    }
}
