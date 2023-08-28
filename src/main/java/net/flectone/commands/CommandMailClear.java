package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.player.PlayerMail;
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
        Main.getDataThreadPool().execute(() ->
                command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(2)) return;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        fPlayer.synchronizeDatabase();

        HashMap<UUID, PlayerMail> mailsList = fPlayer.getMails();

        if (fPlayer.isOnline() || mailsList.isEmpty()) {
            fCommand.sendMeMessage("command.mail-clear.empty");
            return;
        }

        if (!StringUtils.isNumeric(strings[1])) {
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return;

        int number = Integer.parseInt(strings[1]);

        Map.Entry<UUID, PlayerMail> entry = mailsList.entrySet().parallelStream()
                .filter(mailEntry -> mailEntry.getValue().getSender().equals(fCommand.getFPlayer().getUUID()))
                .skip(number - 1)
                .findFirst()
                .orElse(null);

        if (entry == null) {
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return;
        }

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {fPlayer.getRealName(), entry.getValue().getMessage()};

        fPlayer.removeMail(entry.getKey());
        Main.getDatabase().saveMails(fPlayer);

        fCommand.sendMeMessage("command.mail-clear.message", replaceString, replaceTo);
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

                if (!fPlayer.isOnline() && fPlayer.getChatInfo() == null)
                    fPlayer.synchronizeDatabase();

                HashMap<UUID, PlayerMail> mailsList = fPlayer.getMails();

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
