package net.flectone.commands;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.actions.TicTacToe;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.flectone.managers.FileManager.locale;

public class CommandTicTacToe implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(1) || fCommand.getFPlayer() == null || fCommand.getPlayer() == null) {
            return true;
        }

        TicTacToe ticTacToe = TicTacToe.get(strings[0]);
        if (ticTacToe != null && ticTacToe.isEnded()) {
            fCommand.sendMeMessage("command.tic-tac-toe.game.ended");
            return true;
        }

        FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (((secondFPlayer == null || secondFPlayer.getPlayer() == null || !secondFPlayer.isOnline()) && ticTacToe == null) || fCommand.getFPlayer() == secondFPlayer) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return true;
        }

        if (ticTacToe == null) {
            boolean isFirstPlayer = Math.random() > 0.5;

            UUID firstPlayer;
            UUID secondPlayer;

            if (isFirstPlayer) {
                firstPlayer = fCommand.getFPlayer().getUUID();
                secondPlayer = secondFPlayer.getUUID();
            } else {
                firstPlayer = secondFPlayer.getUUID();
                secondPlayer = fCommand.getFPlayer().getUUID();
            }

            int size = getValidSize(strings);
            ticTacToe = new TicTacToe(size, firstPlayer, secondPlayer);

            String sendMessage = locale.getFormatString("command.tic-tac-toe.send-message", fCommand.getPlayer())
                    .replace("<player>", secondFPlayer.getRealName());

            fCommand.getPlayer().sendMessage(sendMessage);

            String getMessage = locale.getFormatString("command.tic-tac-toe.get-message", secondFPlayer.getPlayer())
                    .replace("<player>", fCommand.getFPlayer().getRealName());
            String hoverMessage = locale.getFormatString("command.tic-tac-toe.hover-message", secondFPlayer.getPlayer());

            FComponent textComponent = new FComponent(getMessage)
                    .addRunCommand("/tic-tac-toe " + ticTacToe.getUuid() + " yes")
                    .addHoverText(hoverMessage);

            secondFPlayer.getPlayer().spigot().sendMessage(textComponent.get());

            return true;
        }

        secondFPlayer = ticTacToe.getSecondFPlayer(fCommand.getFPlayer().getUUID());

        if (secondFPlayer == null || secondFPlayer.getPlayer() == null) return true;

        if (!secondFPlayer.isOnline()) {
            fCommand.sendMeMessage("command.null-player");
            ticTacToe.setEnded(true);
            return true;
        }

        if (strings[1].equals("yes") && !ticTacToe.isAccepted()) {
            ticTacToe.setAccepted(true);
            sendMessages(fCommand.getFPlayer(), secondFPlayer, ticTacToe);
            return true;
        }

        if (ticTacToe.isBusy(Integer.parseInt(strings[1])) || !ticTacToe.isNextPlayer(secondFPlayer.getUUID())) {
            fCommand.sendMeMessage("command.tic-tac-toe.game.wrong");
            return true;
        }

        ticTacToe.setMark(fCommand.getFPlayer().getUUID(), Integer.parseInt(strings[1]));

        String message = null;
        if (ticTacToe.hasWinningTrio()) {
            message = locale.getString("command.tic-tac-toe.game.win")
                    .replace("<player>", fCommand.getSenderName());

            ticTacToe.setEnded(true);

        } else if (ticTacToe.checkDraw()) {
            message = locale.getString("command.tic-tac-toe.game.draw");
            ticTacToe.setEnded(true);
        }

        if (message != null) {
            fCommand.getPlayer().sendMessage(ObjectUtil.formatString(message, fCommand.getPlayer()));
            secondFPlayer.getPlayer().sendMessage(ObjectUtil.formatString(message, secondFPlayer.getPlayer()));
        }

        sendMessages(fCommand.getFPlayer(), secondFPlayer, ticTacToe);

        return true;
    }

    private void sendMessages(@NotNull FPlayer firstFPlayer, @NotNull FPlayer secondFPlayer, @NotNull TicTacToe ticTacToe) {
        firstFPlayer.spigotMessage(ticTacToe.build(firstFPlayer));
        secondFPlayer.spigotMessage(ticTacToe.build(secondFPlayer));
    }

    private int getValidSize(@NotNull String[] strings) {
        return strings.length > 1 && StringUtils.isNumeric(strings[1]) && Integer.parseInt(strings[1]) < 10 ? Integer.parseInt(strings[1]) : 3;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> isOnlinePlayer(strings[0]);
            case 2 -> isDigitInArray(strings[1], "", 3, 10);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "tic-tac-toe";
    }
}
