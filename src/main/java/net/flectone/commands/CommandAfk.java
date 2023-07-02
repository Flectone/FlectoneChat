package net.flectone.commands;

import net.flectone.custom.FCommands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.utils.PlayerUtils;

public class CommandAfk implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isHaveCD()) return true;

        sendMessage(fCommand, !fCommand.getFPlayer().isAfk());

        return true;
    }

    public static void setAfkFalse(Player player){
        FPlayer fPlayer = PlayerUtils.getPlayer(player);
        fPlayer.setLastBlock(player.getLocation().getBlock());
        CommandAfk.sendMessage(fPlayer, false);
    }

    public static void sendMessage(FPlayer fPlayer, boolean isAfk){
        FCommands fCommands = new FCommands(fPlayer.getPlayer(), "afk", "afk", new String[]{});
        sendMessage(fCommands, isAfk);
    }

    public static void sendMessage(FCommands fCommands, boolean isAfk){

        FPlayer fPlayer = fCommands.getFPlayer();
        fPlayer.setAfk(isAfk);

        String formatString = Main.config.getFormatString("afk.suffix", fPlayer.getPlayer());

        if(isAfk) fPlayer.addSuffixToName(formatString);
        else fPlayer.removeFromName(Main.config.getFormatString("afk.suffix", fPlayer.getPlayer()));

        fCommands.sendMeMessage("afk.success_" + isAfk);
    }
}
