package net.flectone.commands;

import net.flectone.custom.FCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;

public class CommandPing implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(commandSender instanceof ConsoleCommandSender && strings.length == 0) return true;

        CommandSender player = strings.length > 0 ? Bukkit.getPlayer(strings[0]) : commandSender;

        if(player == null){
            fCommand.sendMeMessage("reply.no_online");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        int currentPing = ((Player) player).getPing();
        int badPing = Main.config.getInt("ping.bad.count");
        int mediumPing = Main.config.getInt("ping.medium.count");
        String pingColor;

        if(currentPing > badPing) pingColor = Main.config.getFormatString("ping.bad.color", commandSender);
        else if (currentPing > mediumPing) pingColor = Main.config.getFormatString("ping.medium.color", commandSender);
        else pingColor = Main.config.getFormatString("ping.good.color", commandSender);

        pingColor += currentPing;

        if(strings.length == 0 || commandSender == player){
            fCommand.sendMeMessage("ping.myself.message", "<ping>", pingColor);
            return true;
        }

        String[] replaceStrings = {"<player>", "<ping>"};
        String[] replaceTos = {player.getName(), pingColor};

        fCommand.sendMeMessage("ping.player.message", replaceStrings, replaceTos);

        return true;
    }
}
