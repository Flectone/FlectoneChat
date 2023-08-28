package net.flectone.misc.entity.info;

import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerWarn {

    private final UUID uuid;
    private final String player;
    private final int time;
    private final String reason;
    private final String moderator;
    private final String playerName;
    private final String moderatorName;

    public PlayerWarn(@NotNull String player, int time, @NotNull String reason, @Nullable String moderator) {
        this(UUID.randomUUID(), player, time, reason, moderator);
    }

    public PlayerWarn(@NotNull UUID uuid, @NotNull String player, int time, @NotNull String reason, @Nullable String moderator) {
        this.uuid = uuid;
        this.player = player;
        this.time = time;
        this.reason = reason;
        this.moderator = moderator;

        this.playerName = getOfflinePlayerName(player);
        this.moderatorName = getOfflinePlayerName(moderator);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getPlayer() {
        return player;
    }

    public String getModerator() {
        return moderator;
    }

    public String getReason() {
        return reason;
    }

    private String getOfflinePlayerName(String uuid) {
        if (uuid == null) return "CONSOLE";

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        String name = offlinePlayer.getName();
        return name != null ? name : "Unknown";
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public int getTime() {
        return time;
    }

    public int getDifferenceTime() {
        return time - ObjectUtil.getCurrentTime();
    }
}
