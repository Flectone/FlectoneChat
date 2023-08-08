package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.actions.Mail;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMail extends FTabCompleter {

    public CommandMail() {
        super.commandName = "mail";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(2) || fCommand.getFPlayer() == null) return true;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        String message = ObjectUtil.toString(strings, 1);

        if (fCommand.isIgnored((Player) commandSender, fPlayer.getOfflinePlayer())) {
            fCommand.sendMeMessage("command.you_ignore");
            return true;
        }

        if (fCommand.isIgnored(fPlayer.getOfflinePlayer(), (Player) commandSender)) {
            fCommand.sendMeMessage("command.he_ignore");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (fPlayer.isOnline()) {
            Bukkit.dispatchCommand(commandSender, "tell " + playerName + " " + message);
            return true;
        }

        Mail mail = new Mail(fCommand.getFPlayer().getUUID(), fPlayer.getUUID(), message);
        mail.setRemoved(false);
        fPlayer.addMail(mail.getUUID(), mail);
        fPlayer.setUpdated(true);

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {fPlayer.getRealName(), message};

        fCommand.sendMeMessage("command.mail.send", replaceString, replaceTo);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isOfflinePlayer(strings[0]);
            case 2 -> isStartsWith(strings[1], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
