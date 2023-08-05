package net.flectone.commands;

import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandUnban extends FTabCompleter {

    public CommandUnban() {
        super.commandName = "unban";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return true;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (!fPlayer.isBanned()) {
            fCommand.sendMeMessage("command.unban.not-banned");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        fPlayer.unban();

        fCommand.sendMeMessage("command.unban.message", "<player>", fPlayer.getRealName());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            FPlayerManager.getBannedPlayers()
                    .forEach(fPlayer -> isStartsWith(strings[0], fPlayer.getRealName()));
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
