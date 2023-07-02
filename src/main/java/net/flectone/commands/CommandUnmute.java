package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandUnmute extends FTabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(!FCommands.isOfflinePlayer(strings[0])){
            fCommand.sendMeMessage("unmute.no_player");
            return true;
        }

        OfflinePlayer mutedPlayer = Bukkit.getOfflinePlayer(strings[0]);

        List<String> list = Main.mutes.getStringList(mutedPlayer.getUniqueId().toString());

        if(list.isEmpty()){
            fCommand.sendMeMessage("unmute.no_muted");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        Main.mutes.updateFile(mutedPlayer.getUniqueId().toString(), null);

        fCommand.sendMeMessage("unmute.success_send", "<player>", mutedPlayer.getName());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            Main.mutes.getKeys().stream().map(string -> Bukkit.getOfflinePlayer(UUID.fromString(string))).collect(Collectors.toSet()).forEach(offlinePlayer -> {
                isStartsWith(strings[0], offlinePlayer.getName());
            });
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
