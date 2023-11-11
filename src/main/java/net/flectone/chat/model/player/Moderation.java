package net.flectone.chat.model.player;

import lombok.Getter;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class Moderation {

    private final String playerUUID;
    private final int time;
    private final String reason;
    private final String moderatorUUID;
    private final Moderation.Type type;
    private int id;

    public Moderation(String player, int time, String reason, String moderator, Moderation.Type type) {
        this.playerUUID = player;
        this.time = time;
        this.reason = reason;
        this.moderatorUUID = moderator;
        this.type = type;
    }

    public Moderation(int id, String player, int time, String reason, String moderator, Moderation.Type type) {
        this(player, time, reason, moderator, type);
        this.id = id;
    }

    public boolean isExpired() {
        return (TimeUtil.getCurrentTime() >= time) && (time != -1);
    }

    @NotNull
    public String getModeratorName() {
        if (moderatorUUID == null) return "CONSOLE";
        return getName(moderatorUUID);
    }

    @NotNull
    public String getPlayerName() {
        return getName(playerUUID);
    }

    @NotNull
    private String getName(@NotNull String uuid) {
        String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
        return name != null ? name : "Unknown";
    }

    public int getRemainingTime() {
        return this.time - TimeUtil.getCurrentTime();
    }

    public enum Type {
        MUTE,
        BAN,
        WARN
    }

}
