package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.player.PlayerMail;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMail implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() ->
                command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(2) || fCommand.getFPlayer() == null) return;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return;
        }

        fPlayer.synchronizeDatabase();

        if (!fPlayer.getChatInfo().getOption("mail")) {
            fCommand.sendMeMessage("command.he-disabled");
            return;
        }

        String message = ObjectUtil.toString(strings, 1);

        if (fCommand.getFPlayer().isIgnored(fPlayer.getUUID())) {
            fCommand.sendMeMessage("command.you_ignore");
            return;
        }

        if (fPlayer.isIgnored(fCommand.getFPlayer().getUUID())) {
            fCommand.sendMeMessage("command.he_ignore");
            return;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return;

        if (fPlayer.isOnline()) {
            fCommand.dispatchCommand("tell " + playerName + " " + message);
            return;
        }

        PlayerMail playerMail = new PlayerMail(fCommand.getFPlayer().getUUID(), fPlayer.getUUID(), message);
        fPlayer.addMail(playerMail.getUUID(), playerMail);

        Main.getDatabase().updateFPlayer(fPlayer, "mails");

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {fPlayer.getRealName(), message};

        fCommand.sendMeMessage("command.mail.send", replaceString, replaceTo);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isOfflinePlayer(strings[0]);
            case 2 -> isTabCompleteMessage(strings[1]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "mail";
    }
}
