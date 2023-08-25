package net.flectone.misc.entity;

import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DatabasePlayer {

    private final String player;
    private final int time;
    private final String reason;
    private final String moderator;

    private final String playerName;
    private final String moderatorName;

    public DatabasePlayer(String player, int time, String reason, String moderator) {
        this.player = player;
        this.time = time;
        this.reason = reason;
        this.moderator = moderator;

        this.playerName = getOfflinePlayerName(player);
        this.moderatorName = getOfflinePlayerName(moderator);
    }

    private String getOfflinePlayerName(String uuid) {
        if (uuid == null) return "CONSOLE";

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        String name = offlinePlayer.getName();
        return name != null ? name : "Unknown";
    }

    public String getPlayer() {
        return player;
    }

    public int getTime() {
        return time;
    }

    public int getDifferenceTime() {
        return time - ObjectUtil.getCurrentTime();
    }

    public String getReason() {
        return reason;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public String getModerator() {
        return moderator;
    }
}
