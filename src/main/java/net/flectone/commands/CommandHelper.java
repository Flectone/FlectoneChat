package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class CommandHelper extends FTabCompleter implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(1)) return true;

        String permission = Main.config.getString("helper.see.permission");

        Set<Player> playerSet = Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.isOp() || player.hasPermission(permission))
                .collect(Collectors.toSet());

        if(playerSet.size() == 0){
            fCommand.sendMeMessage("helper.no_recipients");
            return true;
        }

        String formatMessage = Main.locale.getString("helper.success_get")
                .replace("<player>", fCommand.getSenderName());

        fCommand.sendGlobalMessage(playerSet, formatMessage, ObjectUtil.toString(strings, 0), null, true);

        fCommand.sendMeMessage("helper.success_send");

        return true;
    }
}
