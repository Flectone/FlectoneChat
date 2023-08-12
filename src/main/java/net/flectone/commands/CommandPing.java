package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;

public class CommandPing implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof ConsoleCommandSender && strings.length == 0) return true;

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        FPlayer fPlayer = strings.length > 0
                ? FPlayerManager.getPlayerFromName(strings[0])
                : fCommand.getFPlayer();

        if (fPlayer == null || !fPlayer.isOnline() || fPlayer.getPlayer() == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        int currentPing = fPlayer.getPlayer().getPing();
        int badPing = config.getInt("command.ping.bad.count");
        int mediumPing = config.getInt("command.ping.medium.count");
        String pingColor;

        if (currentPing > badPing) pingColor = config.getFormatString("command.ping.bad.color", commandSender);
        else if (currentPing > mediumPing) pingColor = config.getFormatString("command.ping.medium.color", commandSender);
        else pingColor = config.getFormatString("command.ping.good.color", commandSender);

        pingColor += currentPing;

        if (strings.length == 0 || commandSender == fPlayer.getPlayer()) {
            fCommand.sendMeMessage("command.ping.myself-message", "<ping>", pingColor);
            return true;
        }

        String[] replaceStrings = {"<player>", "<ping>"};
        String[] replaceTos = {fPlayer.getRealName(), pingColor};

        fCommand.sendMeMessage("command.ping.player-message", replaceStrings, replaceTos);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isOnlinePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "ping";
    }
}
