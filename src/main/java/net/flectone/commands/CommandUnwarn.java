package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandUnwarn implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        fPlayer.synchronizeDatabase();

        if (fPlayer.getWarnList().isEmpty() || fPlayer.getRealWarnsCount() == 0) {
            fCommand.sendMeMessage("command.unwarn.not-warned");
            return;
        }

        int index = strings.length == 1 || !StringUtils.isNumeric(strings[1])
                ? 1
                : Integer.parseInt(strings[1]);

        if (index > fPlayer.getWarnList().size() || index < 1) {
            fCommand.sendMeMessage("command.long-number");
            return;
        }

        if (fCommand.isHaveCD()) return;

        index--;
        fPlayer.unwarn(index);

        fCommand.sendMeMessage("command.unwarn.message", "<player>", fPlayer.getRealName());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> Main.getDatabase().getPlayerNameList("warns", "player").parallelStream()
                    .forEach(playerName -> isStartsWith(strings[0], playerName));
            case 2 -> {
                String playerName = strings[0];
                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

                if (fPlayer == null) break;

                if (!fPlayer.isOnline() && fPlayer.getWarnList() == null)
                    fPlayer.synchronizeDatabase();

                if (fPlayer.getWarnList().isEmpty()) break;

                isDigitInArray(strings[1], "", 1, fPlayer.getWarnList().size() + 1);
            }
        }


        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "unwarn";
    }
}
