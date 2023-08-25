package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.info.Mail;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandMailClear implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        HashMap<UUID, Mail> mailsList = fPlayer.getMails();

        if (fPlayer.isOnline() || mailsList.isEmpty()) {
            fCommand.sendMeMessage("command.mail-clear.empty");
            return true;
        }

        if (!StringUtils.isNumeric(strings[1])) {
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        int number = Integer.parseInt(strings[1]);

        Map.Entry<UUID, Mail> entry = mailsList.entrySet().parallelStream()
                .filter(mailEntry -> mailEntry.getValue().getSender().equals(fCommand.getFPlayer().getUUID()))
                .skip(number - 1)
                .findFirst()
                .orElse(null);

        if (entry == null) {
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return true;
        }

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {fPlayer.getRealName(), entry.getValue().getMessage()};

        fPlayer.removeMail(entry.getKey());

        fCommand.sendMeMessage("command.mail-clear.message", replaceString, replaceTo);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();
        if (!(commandSender instanceof Player sender)) return wordsList;

        switch (strings.length) {
            case 1 -> isOfflinePlayer(strings[0]);
            case 2 -> {
                String playerName = strings[0];
                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

                if (fPlayer == null) break;

                HashMap<UUID, Mail> mailsList = fPlayer.getMails();

                if (mailsList.isEmpty()) break;

                int[] counter = {1};
                mailsList.entrySet().parallelStream()
                        .filter(entry -> entry.getValue().getSender().equals(sender.getUniqueId()))
                        .forEach(entry -> isStartsWith(strings[1], String.valueOf(counter[0]++)));
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "mail-clear";
    }
}
