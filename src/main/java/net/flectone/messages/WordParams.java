package net.flectone.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WordParams {


    private boolean isPlayerPing = false;

    private String playerPingName;
    private boolean clickable = false;
    private boolean isItem = false;
    private boolean isUrl = false;
    private String url;
    private boolean isHide;
    private String hideMessage;
    private String text;
    private boolean isFormatted;

    @Nullable
    public String getPlayerPingName() {
        return playerPingName;
    }

    public boolean isPlayerPing() {
        return isPlayerPing;
    }

    public void setPlayerPing(boolean playerPing) {
        isPlayerPing = playerPing;
    }

    public void setClickable(boolean clickable, String playerPingName) {
        this.clickable = clickable;
        this.playerPingName = playerPingName;
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isItem() {
        return isItem;
    }

    public void setItem(boolean item) {
        isItem = item;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public boolean isUrl() {
        return isUrl;
    }

    public void setUrl(String url) {
        isUrl = true;
        this.url = url;
    }

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean hide) {
        isHide = hide;
    }

    @Nullable
    public String getHideMessage() {
        return hideMessage;
    }

    public void setHideMessage(@NotNull String hideMessage) {
        this.hideMessage = hideMessage;
    }

    @NotNull
    public String getText() {
        return text;
    }

    public void setText(@NotNull String text) {
        this.text = text;
    }

    public boolean isFormatted() {
        return isFormatted;
    }

    public void setFormatted(boolean formatted) {
        isFormatted = formatted;
    }

    public boolean isEdited() {
        return isPlayerPing() || isHide() || isUrl() || isClickable() || isItem() || isFormatted();
    }
}
