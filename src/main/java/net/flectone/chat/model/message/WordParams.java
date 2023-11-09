package net.flectone.chat.model.message;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class WordParams {

    private String text;
    private String playerPingName;
    private String urlText;
    private String hideMessage;
    private boolean clickable;
    private boolean isHide;
    private boolean isPlayerPing;
    private boolean isItem;
    private boolean isUrl;
    private boolean isFormatted;
    private boolean isCords;
    private boolean isStats;
    private boolean isPing;

    private final List<String> parameters = new ArrayList<>();

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

    public boolean isEdited() {
        return isPlayerPing() || isHide() || isUrl() || isClickable() || isItem() || isCords() || isStats() || isPing();
    }

    public void addParameters(List<String> parameters) {
        this.parameters.addAll(parameters);
    }

    public boolean contains(String parameter) {
        return parameters.contains(parameter);
    }
}
