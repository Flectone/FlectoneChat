package net.flectone.chat.model.tictactoe;

import net.flectone.chat.component.FComponent;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static net.flectone.chat.manager.FileManager.locale;

public class TicTacToe {

    private static final HashMap<String, TicTacToe> ticTacToeHashMap = new HashMap<>();

    private final String uuid;

    private final String[][] marks;

    private final UUID firstPlayer;

    private final UUID secondPlayer;

    private UUID nextPlayer;

    private boolean isEnded;

    private boolean isAccepted = false;

    public TicTacToe(@NotNull UUID firstPlayer, @NotNull UUID secondPlayer) {
        this.uuid = UUID.randomUUID().toString();
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.nextPlayer = secondPlayer;

        marks = new String[3][3];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.marks[x][y] = "";
            }
        }

        ticTacToeHashMap.put(uuid, this);
    }

    @Nullable
    public static TicTacToe get(String uuid) {
        return ticTacToeHashMap.get(uuid);
    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public void setMark(@NotNull UUID player, int number) {
        int row = (number - 1) / marks.length;
        int column = (number - 1) % marks.length;

        String format = player.equals(firstPlayer)
                ? "first"
                : "second";
        marks[row][column] = format;
        nextPlayer = player.equals(firstPlayer) ? firstPlayer : secondPlayer;
    }

    @Nullable
    public FPlayer getSecondFPlayer(@NotNull UUID player) {
        return FPlayerManager.get(player.equals(firstPlayer) ? secondPlayer : firstPlayer);
    }

    public boolean isBusy(int number) {
        int row = (number - 1) / marks.length;
        int column = (number - 1) % marks.length;
        return !marks[row][column].isEmpty();
    }

    public boolean isNextPlayer(@NotNull UUID player) {
        return nextPlayer.equals(player);
    }

    @Nullable
    public FPlayer getCurrentFPlayer() {
        return FPlayerManager.get(nextPlayer.equals(firstPlayer) ? secondPlayer : firstPlayer);
    }

    @NotNull
    public BaseComponent[] build(@NotNull FPlayer sender) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        if (!isEnded) {
            componentBuilder.append("\n");
            String moveMessage = locale.getVaultString(sender.getPlayer(), "commands.tictactoe.game.move")
                    .replace("<player>", getCurrentFPlayer().getMinecraftName());

            moveMessage = MessageUtil.formatAll(sender.getPlayer(), moveMessage);

            componentBuilder
                    .append(FComponent.fromLegacyText(moveMessage))
                    .append("\n");
        }

        boolean isNext = isNextPlayer(sender.getUuid());

        int k = 0;
        for (String[] row : marks) {
            for (String mark : row) {

                if (mark.isEmpty()) {
                    mark = locale.getVaultString(sender.getPlayer(), "commands.tictactoe.format.empty");
                } else if (mark.equals("first") || mark.equals("second")) {
                    mark = locale.getVaultString(sender.getPlayer(), "commands.tictactoe.format." + mark);
                }

                k++;
                FComponent textComponent = new FComponent(MessageUtil.formatAll(sender.getPlayer(), mark));
                if (!isNext) textComponent.addRunCommand("/ttt " + uuid + " " + k);

                componentBuilder.append(textComponent.get());
            }
            componentBuilder.append("\n");
        }


        return componentBuilder.create();
    }

    public boolean hasWinningTrio(@NotNull Player sender) {
        for (int row = 0; row < 3; row++) {
            if (!marks[row][0].isEmpty() && marks[row][0].equals(marks[row][1]) && marks[row][1].equals(marks[row][2])) {
                setWinMark(sender, row, 0);
                setWinMark(sender, row, 1);
                setWinMark(sender, row, 2);
                return true;
            }
        }

        for (int col = 0; col < 3; col++) {
            if (!marks[0][col].isEmpty() && marks[0][col].equals(marks[1][col]) && marks[1][col].equals(marks[2][col])) {
                setWinMark(sender, 0, col);
                setWinMark(sender, 1, col);
                setWinMark(sender, 2, col);
                return true;
            }
        }

        if (!marks[1][1].isEmpty() && marks[0][0].equals(marks[1][1]) && marks[1][1].equals(marks[2][2])) {
            setWinMark(sender, 0, 0);
            setWinMark(sender, 1, 1);
            setWinMark(sender, 2, 2);
            return true;
        }

        if (!marks[1][1].isEmpty() && marks[0][2].equals(marks[1][1]) && marks[1][1].equals(marks[2][0])) {
            setWinMark(sender, 0, 2);
            setWinMark(sender, 1, 1);
            setWinMark(sender, 2, 0);
            return true;
        }

        return false;
    }

    private void setWinMark(@NotNull Player player, int row, int col) {
        String mark = locale.getVaultString(player, "commands.tictactoe.format." + marks[row][col]);
        mark = MessageUtil.formatAll(player, mark);

        marks[row][col] = locale.getVaultString(player, "commands.tictactoe.format.win") + ChatColor.stripColor(mark);
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
    }

    public boolean checkDraw() {
        for (String[] strings : marks) {
            for (int y = 0; y < marks.length; y++) {
                if (strings[y].isEmpty()) return false;
            }
        }
        return true;
    }
}