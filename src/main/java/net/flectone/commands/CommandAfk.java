package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.custom.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandAfk extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isHaveCD()) return true;

        sendMessage(fCommand, !fCommand.getFPlayer().isAfk());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return wordsList;
    }

    public static void setAfkFalse(Player player){
        FPlayer afkPlayer = FPlayerManager.getPlayer(player);
        afkPlayer.setBlock(player.getLocation().getBlock());
        CommandAfk.sendMessage(afkPlayer, false);
    }

    public static void sendMessage(FPlayer afkFPlayer, boolean isAfk){
        FCommands fCommands = new FCommands(afkFPlayer.getPlayer(), "afk", "afk", new String[]{});
        sendMessage(fCommands, isAfk);
    }

    public static void sendMessage(FCommands fCommands, boolean isAfk){
        FPlayer fPlayer = fCommands.getFPlayer();
        fPlayer.setAfk(isAfk);

        String formatString = isAfk ? Main.locale.getFormatString("command.afk.suffix", fPlayer.getPlayer()) : "";

        fPlayer.setAfkSuffix(formatString);

        fCommands.sendMeMessage("command.afk." + isAfk + "-message");
    }
}
