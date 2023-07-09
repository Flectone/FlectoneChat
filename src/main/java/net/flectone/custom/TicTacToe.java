package net.flectone.custom;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class TicTacToe {

    private static final HashMap<String, TicTacToe> ticTacToeHashMap = new HashMap<>();

    private static final String mark = Main.locale.getString("command.tic-tac-toe.format.empty");

    private final String uuid;

    private final String[][] marks;

    private String firstPlayer;

    private String secondPlayer;

    private String nextPlayer;

    private boolean isEnded;

    private boolean isAccepted = false;

    public TicTacToe(int size, String firstPlayer, String secondPlayer){
        this.uuid = UUID.randomUUID().toString();
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.nextPlayer = secondPlayer;

        marks = new String[size][size];

        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                this.marks[x][y] = mark;
            }
        }

        ticTacToeHashMap.put(uuid, this);
    }

    public String getUuid() {
        return uuid;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setMark(String player, int number){
        int row = (number - 1) / marks.length;
        int column = (number - 1) % marks.length;

        String format = player.equals(firstPlayer) ? Main.locale.getString("command.tic-tac-toe.format.first") : Main.locale.getString("command.tic-tac-toe.format.second");
        marks[row][column] = format;
        nextPlayer = player.equals(firstPlayer) ? firstPlayer : secondPlayer;
    }

    public FPlayer getSecondFPlayer(String player){
        return FPlayerManager.getPlayer(player.equals(firstPlayer) ? secondPlayer : firstPlayer);
    }

    public boolean isBusy(int number){
        int row = (number - 1) / marks.length;
        int column = (number - 1) % marks.length;
        return !marks[row][column].equals(mark);
    }

    public boolean isNextPlayer(String player){
        return nextPlayer.equals(player);
    }

    public FPlayer getCurrentFPlayer(){
        return FPlayerManager.getPlayer(nextPlayer.equals(firstPlayer) ? secondPlayer : firstPlayer);
    }

    public BaseComponent[] build(FPlayer fPlayer){
        ComponentBuilder componentBuilder = new ComponentBuilder();

        if(!isEnded){
            componentBuilder.append("\n");
            String moveMessage = (Main.locale.getFormatString("command.tic-tac-toe.game.move", fPlayer.getPlayer())
                    .replace("<player>", getCurrentFPlayer().getRealName()));

            componentBuilder
                    .append(TextComponent.fromLegacyText(moveMessage))
                    .append("\n");
        }

        boolean isNext = isNextPlayer(fPlayer.getUUID());

        int k = 0;
        for(String[] row : marks){
            for(String mark : row){
                k++;
                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(ObjectUtil.formatString(mark, fPlayer.getPlayer())));
                if(!isNext) textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ttt " + uuid + " " + k));

                componentBuilder.append(textComponent);
            }
            componentBuilder.append("\n");
        }


        return componentBuilder.create();
    }

    public static TicTacToe get(String uuid){
        return ticTacToeHashMap.get(uuid);
    }

    public boolean hasWinningTrio() {
        int rows = marks.length;
        int cols = marks[0].length;
        String winColor = Main.locale.getString("command.tic-tac-toe.format.win");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 2; j++) {
                if (!marks[i][j].equals(mark) && marks[i][j].equals(marks[i][j + 1]) && marks[i][j].equals(marks[i][j + 2])) {
                    String filledMark = winColor + marks[i][j].substring(marks[i][j].length() - 1);
                    marks[i][j] = filledMark;
                    marks[i][j + 1] = filledMark;
                    marks[i][j + 2] = filledMark;
                    return true;
                }
            }
        }

        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < cols; j++) {
                if (!marks[i][j].equals(mark) && Objects.equals(marks[i][j], marks[i + 1][j]) && Objects.equals(marks[i][j], marks[i + 2][j])) {
                    String filledMark = winColor + marks[i][j].substring(marks[i][j].length() - 1);
                    marks[i][j] = filledMark;
                    marks[i + 1][j] = filledMark;
                    marks[i + 2][j] = filledMark;
                    return true;
                }
            }
        }

        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < cols - 2; j++) {
                if (!marks[i][j].equals(mark) && Objects.equals(marks[i][j], marks[i + 1][j + 1]) && Objects.equals(marks[i][j], marks[i + 2][j + 2])) {
                    String filledMark = winColor + marks[i][j].substring(marks[i][j].length() - 1);
                    marks[i][j] = filledMark;
                    marks[i + 1][j + 1] = filledMark;
                    marks[i + 2][j + 2] = filledMark;
                    return true;
                }
            }
        }

        for (int i = 0; i < rows - 2; i++) {
            for (int j = 2; j < cols; j++) {
                if (!marks[i][j].equals(mark) && Objects.equals(marks[i][j], marks[i + 1][j - 1]) && Objects.equals(marks[i][j], marks[i + 2][j - 2])) {
                    String filledMark = winColor + marks[i][j].substring(marks[i][j].length() - 1);
                    marks[i][j] = filledMark;
                    marks[i + 1][j - 1] = filledMark;
                    marks[i + 2][j - 2] = filledMark;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
    }

    public boolean checkDraw(){
        for (String[] strings : marks) {
            for (int y = 0; y < marks.length; y++) {
                if (strings[y].equals(mark)) return false;
            }
        }
        return true;
    }
}
