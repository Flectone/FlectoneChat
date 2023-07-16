package net.flectone.messages;

public class WordParams {


    private boolean isPlayerPing = false;

    private String playerPingName;


    public String getPlayerPingName() {
        return playerPingName;
    }

    public boolean isPlayerPing() {
        return isPlayerPing;
    }

    public void setPlayerPing(boolean playerPing) {
        isPlayerPing = playerPing;
    }

    private boolean clickable = false;

    public void setClickable(boolean clickable, String playerPingName) {
        this.clickable = clickable;
        this.playerPingName = playerPingName;
    }

    public boolean isClickable() {
        return clickable;
    }

    private boolean isItem = false;

    public void setItem(boolean item) {
        isItem = item;
    }

    public boolean isItem() {
        return isItem;
    }

    private boolean isUrl = false;
    
    private String url;

    public void setUrl(String url) {
        isUrl = true;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isUrl() {
        return isUrl;
    }

    private boolean isHide;

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean hide) {
        isHide = hide;
    }

    private String hideMessage;

    public void setHideMessage(String hideMessage) {
        this.hideMessage = hideMessage;
    }

    public String getHideMessage() {
        return hideMessage;
    }

    private String text;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    private boolean isFormatted;

    public void setFormatted(boolean formatted) {
        isFormatted = formatted;
    }

    public boolean isFormatted() {
        return isFormatted;
    }

    public boolean isEdited(){
        return isPlayerPing() || isHide() || isUrl() || isClickable() || isItem() || isFormatted();
    }
}
