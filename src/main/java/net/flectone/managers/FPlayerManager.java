package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.entity.FEntity;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FPlayerManager {

    private static final Collection<FPlayer> PLAYERS = new ArrayList<>();
    private static final Set<FPlayer> BANNED_PLAYERS = new HashSet<>();
    private static final Set<FPlayer> MUTED_PLAYERS = new HashSet<>();

    private static Scoreboard scoreBoard;

    public static void setScoreBoard() {
        scoreBoard = Main.config.getBoolean("scoreboard.custom") ?
                Bukkit.getScoreboardManager().getNewScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @NotNull
    public static Scoreboard getScoreBoard() {
        return scoreBoard;
    }

    @NotNull
    public static Collection<FPlayer> getPlayers() {
        return PLAYERS;
    }

    @NotNull
    public static Set<FPlayer> getBannedPlayers() {
        return BANNED_PLAYERS;
    }

    @NotNull
    public static Set<FPlayer> getMutedPlayers() {
        return MUTED_PLAYERS;
    }

    public static void loadPlayers() {
        Arrays.stream(Bukkit.getOfflinePlayers())
                .forEach(FPlayerManager::addPlayer);

        Main.getDatabase().loadDatabase();

        Bukkit.getOnlinePlayers().parallelStream()
                .forEach(player -> {
                    FPlayer fPlayer = getPlayer(player);
                    if (fPlayer == null) return;
                    fPlayer.initialize(player);
                });
    }

    public static void loadBanList() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        Bukkit.getBannedPlayers().parallelStream()
                .forEach(offlinePlayer -> {
                    FPlayer fPlayer = FPlayerManager.getPlayer(offlinePlayer);
                    if (fPlayer == null || offlinePlayer.getName() == null) return;

                    BanEntry banEntry = banList.getBanEntry(offlinePlayer.getName());
                    if (banEntry == null) return;

                    String reason = banEntry.getReason() != null
                            ? banEntry.getReason()
                            : Main.locale.getString("command.tempban.default-reason");

                    fPlayer.tempban(-1, reason);

                    banList.pardon(offlinePlayer.getName());
                });
    }

    public static void uploadPlayers() {
        PLAYERS.stream()
                .filter(FPlayer::isUpdated)
                .forEach(fPlayer -> {
                    Main.getDatabase().uploadDatabase(fPlayer);
                    fPlayer.setUpdated(false);
                });
    }

    public static void removePlayersFromTeams() {
        PLAYERS.forEach(FEntity::removePlayerFromTeam);
    }

    public static void addPlayer(@NotNull OfflinePlayer offlinePlayer) {
        FPlayer fPlayer = new FPlayer(offlinePlayer);
        if (!PLAYERS.contains(fPlayer))
            PLAYERS.add(fPlayer);
    }


    public static FPlayer addPlayer(@NotNull Player player) {
        FPlayer newPlayer = new FPlayer(player);

        if (PLAYERS.contains(newPlayer)) {

            FPlayer fPlayer = getPlayer(player);
            if (fPlayer == null) return null;

            fPlayer.initialize(player);
            return fPlayer;
        }

        PLAYERS.add(newPlayer);
        newPlayer.initialize(player);
        Main.getDatabase().setPlayer(newPlayer.getUUID());
        return newPlayer;
    }

    @Nullable
    public static FPlayer getPlayerFromName(@NotNull String name) {
        return PLAYERS.parallelStream()
                .filter(fPlayer -> fPlayer.getRealName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull OfflinePlayer offlinePlayer) {
        return getPlayer(offlinePlayer.getUniqueId());
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull String uuid) {
        return PLAYERS.stream()
                .filter(player -> player.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull UUID uuid) {
        return getPlayer(uuid.toString());
    }

    public static void removePlayer(@NotNull String uuid) {
        PLAYERS.remove(getPlayer(uuid));
    }

    public static void removePlayer(@NotNull UUID uuid) {
        removePlayer(uuid.toString());
    }

    public static void removePlayer(@NotNull Player player) {
        removePlayer(player.getUniqueId());
    }
}
