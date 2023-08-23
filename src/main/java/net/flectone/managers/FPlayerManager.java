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

import static net.flectone.managers.FileManager.locale;
import static net.flectone.managers.FileManager.config;

public class FPlayerManager {

    private static final HashMap<UUID, FPlayer> fPlayerHashMap = new HashMap<>();
    private static final Set<FPlayer> bannedPlayers = new HashSet<>();
    private static final Set<FPlayer> mutedPlayers = new HashSet<>();
    private static Scoreboard scoreBoard;

    public static void setScoreBoard() {
        scoreBoard = config.getBoolean("scoreboard.custom") ?
                Bukkit.getScoreboardManager().getNewScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @NotNull
    public static Scoreboard getScoreBoard() {
        return scoreBoard;
    }

    @NotNull
    public static Collection<FPlayer> getPlayers() {
        return fPlayerHashMap.values();
    }

    @NotNull
    public static Set<FPlayer> getBannedPlayers() {
        return bannedPlayers;
    }

    @NotNull
    public static Set<FPlayer> getMutedPlayers() {
        return mutedPlayers;
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
                            : locale.getString("command.tempban.default-reason");

                    fPlayer.tempban(-1, reason);

                    banList.pardon(offlinePlayer.getName());
                });
    }

    public static void uploadPlayers() {
        fPlayerHashMap.values().stream()
                .filter(FPlayer::isUpdated)
                .forEach(fPlayer -> {
                    Main.getDatabase().uploadDatabase(fPlayer);
                    fPlayer.setUpdated(false);
                });
    }

    public static void removePlayersFromTeams() {
        fPlayerHashMap.values().forEach(FEntity::removePlayerFromTeam);
    }

    public static void addPlayer(@NotNull OfflinePlayer offlinePlayer) {
        UUID uuid = offlinePlayer.getUniqueId();
        if (fPlayerHashMap.containsKey(uuid)) return;

        FPlayer fPlayer = new FPlayer(offlinePlayer);
        fPlayerHashMap.put(uuid, fPlayer);
    }


    public static FPlayer addPlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (fPlayerHashMap.containsKey(uuid)) {
            FPlayer fPlayer = getPlayer(uuid);
            if (fPlayer == null) return null;

            fPlayer.initialize(player);
            return fPlayer;
        }

        FPlayer fPlayer = new FPlayer(player);
        fPlayerHashMap.put(uuid, fPlayer);
        fPlayer.initialize(player);
        Main.getDatabase().setPlayer(fPlayer.getUUID());
        return fPlayer;
    }

    @Nullable
    public static FPlayer getPlayerFromName(@NotNull String name) {
        return fPlayerHashMap.values()
                .parallelStream()
                .filter(fPlayer -> fPlayer != null && fPlayer.getRealName().equals(name))
                .findFirst().orElse(null);
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
    public static FPlayer getPlayer(@NotNull UUID uuid) {
        return fPlayerHashMap.get(uuid);
    }


    public static void removePlayer(@NotNull UUID uuid) {
        fPlayerHashMap.remove(uuid);
    }

    public static void removePlayer(@NotNull Player player) {
        removePlayer(player.getUniqueId());
    }

}
