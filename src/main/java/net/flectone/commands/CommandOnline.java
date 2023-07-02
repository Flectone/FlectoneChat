package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        if(command.getName().equalsIgnoreCase("lastonline")){
            Player player = Bukkit.getPlayer(playerName);

            if(player != null){
                fCommand.sendMeMessage("lastonline.message_now", "<player>", player.getName());
                return true;
            }
        }

        if(!FCommands.isOfflinePlayer(playerName)){
            fCommand.sendMeMessage("online.no_player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        long playedTime = command.getName().equalsIgnoreCase("lastonline") ? offlinePlayer.getLastPlayed() : offlinePlayer.getFirstPlayed();

        String[] replacedStrings = {"<player>", "<time>"};
        String[] replacedToStrings = {offlinePlayer.getName(), ObjectUtil.convertTimeToString(playedTime)};

        fCommand.sendMeMessage(command.getName() + ".message", replacedStrings, replacedToStrings);

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
