package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandPing extends FTabCompleter {

    public CommandPing() {
        super.commandName = "ping";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (commandSender instanceof ConsoleCommandSender && strings.length == 0) return true;

        FPlayer fPlayer = strings.length > 0 ? FPlayerManager.getPlayerFromName(strings[0]) : fCommand.getFPlayer();

        if (fPlayer == null || !fPlayer.isOnline()) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        int currentPing = fPlayer.getPlayer().getPing();
        int badPing = Main.config.getInt("command.ping.bad.count");
        int mediumPing = Main.config.getInt("command.ping.medium.count");
        String pingColor;

        if (currentPing > badPing) pingColor = Main.config.getFormatString("command.ping.bad.color", commandSender);
        else if (currentPing > mediumPing)
            pingColor = Main.config.getFormatString("command.ping.medium.color", commandSender);
        else pingColor = Main.config.getFormatString("command.ping.good.color", commandSender);

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
}
