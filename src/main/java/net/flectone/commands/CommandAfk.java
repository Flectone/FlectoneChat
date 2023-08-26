package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class CommandAfk implements FTabCompleter {

    public static void setAfkFalse(@NotNull Player player) {
        FPlayer afkPlayer = FPlayerManager.getPlayer(player);
        if(afkPlayer == null) return;
        afkPlayer.setBlock(player.getLocation().getBlock());

        CommandAfk.sendMessage(afkPlayer, false);
    }

    public static void sendMessage(@NotNull FPlayer afkFPlayer, boolean isAfk) {
        if(afkFPlayer.getPlayer() == null) return;
        FCommand fCommand = new FCommand(afkFPlayer.getPlayer(), "afk", "afk", new String[]{});
        setAfkAndSendMessage(fCommand, isAfk);
    }

    private static void setAfkAndSendMessage(@NotNull FCommand fCommand, boolean isAfk) {
        FPlayer fPlayer = fCommand.getFPlayer();
        if(fPlayer == null) return;
        fPlayer.setAfk(isAfk);

        String afkSuffix = isAfk
                ? locale.getFormatString("command.afk.suffix", fPlayer.getPlayer())
                : "";

        fPlayer.setAfkSuffix(afkSuffix);

        fCommand.sendMeMessage("command.afk." + isAfk + "-message");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isHaveCD() || fCommand.getFPlayer() == null) return true;

        setAfkAndSendMessage(fCommand, !fCommand.getFPlayer().isAfk());
        fCommand.getFPlayer().updateName();

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();
        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "afk";
    }
}
