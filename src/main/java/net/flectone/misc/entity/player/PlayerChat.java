package net.flectone.misc.entity.player;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

public class PlayerChat {

    private final String player;
    private String chatType;

    private final HashMap<String, Boolean> options;

    public PlayerChat(@NotNull String playerUUID) {
        this.player = playerUUID;
        this.options = new HashMap<>();
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    @NotNull
    public Set<String> getOptionsList() {
        return options.keySet();
    }

    public void setOption(@NotNull String optionName, boolean bool) {
        options.put(optionName, bool);
    }

    @NotNull
    public String getChatType() {
        return chatType;
    }

    public boolean getOption(@NotNull String optionName) {
        if (!getOptionsList().contains(optionName)) return true;
        return options.get(optionName);
    }

    @NotNull
    public String getOptionString(@NotNull String optionName) {
        return String.valueOf(options.get(optionName));
    }

    public void setChatType(@NotNull String chatType) {
        this.chatType = chatType;
    }
}
