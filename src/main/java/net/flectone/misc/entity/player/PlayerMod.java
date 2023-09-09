package net.flectone.misc.entity.player;

import net.flectone.utils.ObjectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerMod {

    private final String player;
    private final int time;
    private final String reason;
    private final String moderator;

    private final String playerName;
    private final String moderatorName;

    public PlayerMod(@NotNull String player, int time, @NotNull String reason, @Nullable String moderator) {
        this.player = player;
        this.time = time;
        this.reason = reason;
        this.moderator = moderator;

        this.playerName = ObjectUtil.getOfflinePlayerName(player);
        this.moderatorName = ObjectUtil.getOfflinePlayerName(moderator);
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    public int getTime() {
        return time;
    }

    public int getDifferenceTime() {
        return time - ObjectUtil.getCurrentTime();
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

    @Nullable
    public String getModerator() {
        return moderator;
    }

    public boolean isExpired() {
        return getTime() != -1 && getDifferenceTime() <= 0;
    }
}
