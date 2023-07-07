package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandUnmute extends FTabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if(fPlayer == null){
            fCommand.sendMeMessage("unmute.no_player");
            return true;
        }

        if(fPlayer.getMuteTime() == 0){
            fCommand.sendMeMessage("unmute.no_muted");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        fCommand.getFPlayer().setMuteTime(0);
        fCommand.getFPlayer().setMuteReason("");

        fCommand.sendMeMessage("unmute.success_send", "<player>", fPlayer.getRealName());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            Main.getDatabase().getAllMutedPlayers().forEach(uuid ->
                    isStartsWith(strings[0], Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()));
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
