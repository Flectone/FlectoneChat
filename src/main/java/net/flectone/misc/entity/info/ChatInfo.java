package net.flectone.misc.entity.info;

import java.util.HashMap;
import java.util.Set;

public class ChatInfo {

    private final String player;
    private String chatType;

    private final HashMap<String, Boolean> options;

    public ChatInfo(String playerUUID) {
        this.player = playerUUID;
        this.options = new HashMap<>();
    }

    public String getPlayer() {
        return player;
    }

    public Set<String> getOptionsList() {
        return options.keySet();
    }

    public void setOption(String optionName, boolean bool) {
        options.put(optionName, bool);
    }

    public String getChatType() {
        return chatType;
    }

    public boolean getOption(String optionName) {
        if (!getOptionsList().contains(optionName)) return true;
        return options.get(optionName);
    }

    public String getOptionString(String optionName) {
        return String.valueOf(options.get(optionName));
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}
