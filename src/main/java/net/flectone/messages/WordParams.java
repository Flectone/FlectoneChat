package net.flectone.messages;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
    private boolean isCords;

    private boolean isStats;

    public void setCords(boolean cords) {
        isCords = cords;
    }

    public boolean isCords() {
        return isCords;
    }

    public void setStats(boolean stats) {
        isStats = stats;
    }

    public boolean isStats() {
        return isStats;
    }

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

    public String getFormatting() {
        return getChatColor("**") + getChatColor("__") + getChatColor("##") + getChatColor("~~") + getChatColor("??");
    }

    public String getChatColor(String param) {
        if (!parameters.contains(param)) return "";

        return switch (param) {
            case "**" -> String.valueOf(ChatColor.BOLD);
            case "__" -> String.valueOf(ChatColor.UNDERLINE);
            case "##" -> String.valueOf(ChatColor.ITALIC);
            case "~~" -> String.valueOf(ChatColor.STRIKETHROUGH);
            case "??" -> String.valueOf(ChatColor.MAGIC);
            default -> "";
        };
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
        return isPlayerPing() || isHide() || isUrl() || isClickable() || isItem() || isFormatted() || isCords() || isStats();
    }

    private final List<String> parameters = new ArrayList<>();

    public void addParameters(List<String> parameters) {
        this.parameters.addAll(parameters);
    }

    public boolean contains(String parameter) {
        return parameters.contains(parameter);
    }

    public List<String> getParameters() {
        return parameters;
    }
}
