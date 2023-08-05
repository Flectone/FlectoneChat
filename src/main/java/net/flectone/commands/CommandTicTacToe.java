package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.actions.TicTacToe;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandTicTacToe extends FTabCompleter {

    public CommandTicTacToe() {
        super.commandName = "tic-tac-toe";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage() || fCommand.isInsufficientArgs(1)) {
            return true;
        }

        TicTacToe ticTacToe = TicTacToe.get(strings[0]);
        if (ticTacToe != null && ticTacToe.isEnded()) {
            fCommand.sendMeMessage("command.tic-tac-toe.game.ended");
            return true;
        }

        FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (((secondFPlayer == null || !secondFPlayer.isOnline()) && ticTacToe == null) || fCommand.getFPlayer() == secondFPlayer) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (ticTacToe == null) {
            boolean isFirstPlayer = Math.random() > 0.5;

            String firstPlayer = isFirstPlayer ? fCommand.getFPlayer().getUUID() : secondFPlayer.getUUID();
            String secondPlayer = isFirstPlayer ? secondFPlayer.getUUID() : fCommand.getFPlayer().getUUID();

            int size = getValidSize(strings);
            ticTacToe = new TicTacToe(size, firstPlayer, secondPlayer);

            String sendMessage = Main.locale.getFormatString("command.tic-tac-toe.send-message", fCommand.getPlayer())
                    .replace("<player>", secondFPlayer.getRealName());

            fCommand.getPlayer().sendMessage(sendMessage);

            String getMessage = Main.locale.getFormatString("command.tic-tac-toe.get-message", secondFPlayer.getPlayer())
                    .replace("<player>", fCommand.getFPlayer().getRealName());
            String hoverMessage = Main.locale.getFormatString("command.tic-tac-toe.hover-message", secondFPlayer.getPlayer());

            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(getMessage));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ttt " + ticTacToe.getUuid() + " yes"));
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverMessage)));

            secondFPlayer.getPlayer().spigot().sendMessage(textComponent);

            return true;
        }

        secondFPlayer = ticTacToe.getSecondFPlayer(fCommand.getFPlayer().getUUID());

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
            message = Main.locale.getString("command.tic-tac-toe.game.win")
                    .replace("<player>", fCommand.getSenderName());

            ticTacToe.setEnded(true);

        } else if (ticTacToe.checkDraw()) {
            message = Main.locale.getString("command.tic-tac-toe.game.draw");
            ticTacToe.setEnded(true);
        }

        if (message != null) {
            fCommand.getPlayer().sendMessage(ObjectUtil.formatString(message, fCommand.getPlayer()));
            secondFPlayer.getPlayer().sendMessage(ObjectUtil.formatString(message, secondFPlayer.getPlayer()));
        }

        sendMessages(fCommand.getFPlayer(), secondFPlayer, ticTacToe);

        return true;
    }

    private void sendMessages(FPlayer firstFPlayer, FPlayer secondFPlayer, TicTacToe ticTacToe) {
        firstFPlayer.getPlayer().spigot().sendMessage(ticTacToe.build(firstFPlayer));
        secondFPlayer.getPlayer().spigot().sendMessage(ticTacToe.build(secondFPlayer));
    }

    private int getValidSize(String[] strings) {
        return strings.length > 1 && StringUtils.isNumeric(strings[1]) && Integer.parseInt(strings[1]) < 10 ? Integer.parseInt(strings[1]) : 3;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isOnlinePlayer(strings[0]);
        } else if (strings.length == 2) {
            for (int x = 3; x < 10; x++) {
                isStartsWith(strings[1], String.valueOf(x));
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
