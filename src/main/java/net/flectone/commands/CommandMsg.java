package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandMsg implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() ->
                command(commandSender, command, s, strings));

        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return;

        String playerName = strings[0];
        FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(playerName);
        if (secondFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return;

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return;
        }

        String message = ObjectUtil.toString(strings, 1);

        if (!secondFPlayer.isOnline() && config.getBoolean("command.mail.enable")) {
            fCommand.dispatchCommand("mail " + playerName + " " + message);
            return;
        }

        if (!secondFPlayer.getChatInfo().getOption("msg")) {
            fCommand.sendMeMessage("command.he-disabled");
            return;
        }

        if (!fCommand.isConsole()) {
            if (fCommand.getSenderName().equalsIgnoreCase(playerName)) {
                commandSender.sendMessage(locale.getFormatString("command.msg.myself", commandSender) + message);
                return;
            }

            if (fCommand.getFPlayer() != null && fCommand.getFPlayer().isIgnored(secondFPlayer.getPlayer())) {
                fCommand.sendMeMessage("command.you_ignore");
                return;
            }

            if (secondFPlayer.isIgnored((Player) commandSender)) {
                fCommand.sendMeMessage("command.he_ignore");
                return;
            }
        }

        if(secondFPlayer.getPlayer() == null) return;

        fCommand.sendTellMessage(commandSender, secondFPlayer.getPlayer(), message);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isConfigOnlineModePlayer(strings[0]);
            case 2 -> isTabCompleteMessage(strings[1]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "msg";
    }
}
