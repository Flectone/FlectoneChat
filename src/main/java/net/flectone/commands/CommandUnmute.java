package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandUnmute implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(1)) return true;

        if(!FCommands.isRealOfflinePlayer(strings[0])){
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

        Main.mutes.set(mutedPlayer.getUniqueId().toString(), null);
        Main.mutes.saveFile();

        fCommand.sendMeMessage("unmute.success_send");

        return true;
    }
}
