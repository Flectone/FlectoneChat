package net.flectone.misc.entity;

import java.util.HashMap;

public class PlayerChatInfo {

    private String chatType;

    private boolean advancements;
    private boolean deaths;
    private boolean joins;
    private boolean quits;
    private boolean commandMe;
    private boolean commandTry;
    private boolean commandTryCube;
    private boolean commandBall;
    private boolean commandTempban;
    private boolean commandMute;
    private boolean commandWarn;
    private boolean commandMsg;
    private boolean commandReply;
    private boolean commandMail;
    private boolean commandTicTacToe;

    private final HashMap<String, Boolean> options;

    public PlayerChatInfo() {
        this.options = new HashMap<>();
    }

    public void setOption(String optionName, boolean bool) {
        options.put(optionName, bool);
    }

    public String getChatType() {
        return chatType;
    }

    public boolean getOption(String optionName) {
        return options.get(optionName);
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}
