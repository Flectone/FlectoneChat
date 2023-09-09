package net.flectone.misc.entity.player;

import net.flectone.utils.ObjectUtil;
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

        this.playerName = ObjectUtil.getOfflinePlayerName(player);
        this.moderatorName = ObjectUtil.getOfflinePlayerName(moderator);
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    @Nullable
    public String getModerator() {
        return moderator;
    }

    @NotNull
    public String getReason() {
        return reason;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    @NotNull
    public String getModeratorName() {
        return moderatorName;
    }

    public int getTime() {
        return time;
    }

    public int getDifferenceTime() {
        return time - ObjectUtil.getCurrentTime();
    }

    public boolean isExpired() {
        return getTime() != -1 && getDifferenceTime() <= 0;
    }
}
