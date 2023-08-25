package net.flectone.misc.entity.info;

import java.util.HashMap;

public class ChatInfo {

    private String chatType;

    private final HashMap<String, Boolean> options;

    public ChatInfo() {
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
