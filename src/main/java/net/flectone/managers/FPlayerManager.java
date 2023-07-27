package net.flectone.managers;

import net.flectone.Main;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FPlayerManager {

    private static Scoreboard scoreBoard;

    private static final HashMap<String, FPlayer> fPlayerHashMap = new HashMap<>();

    private static final Set<FPlayer> bannedPlayers = new HashSet<>();

    private static final Set<FPlayer> mutedPlayers = new HashSet<>();

    public static void setScoreBoard() {
        scoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public static Scoreboard getScoreBoard() {
        return scoreBoard;
    }

    public static Collection<FPlayer> getPlayers() {
        return fPlayerHashMap.values();
    }

    public static Set<FPlayer> getBannedPlayers() {
        return bannedPlayers;
    }

    public static Set<FPlayer> getMutedPlayers() {
        return mutedPlayers;
    }

    public static void loadPlayers() {
        Arrays.stream(Bukkit.getOfflinePlayers())
                .forEach(FPlayerManager::addPlayer);

        Main.getDatabase().loadDatabase();

        Bukkit.getOnlinePlayers().parallelStream()
                .forEach(player -> getPlayer(player).initialize(player));
    }

    public static void loadBanList() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        Bukkit.getBannedPlayers().parallelStream()
                .forEach(offlinePlayer -> {
                    FPlayer fPlayer = FPlayerManager.getPlayer(offlinePlayer);
                    if (fPlayer == null || offlinePlayer.getName() == null) return;

                    String reason = banList.getBanEntry(offlinePlayer.getName()).getReason();
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
        String uuid = offlinePlayer.getUniqueId().toString();
        if (fPlayerHashMap.containsKey(uuid)) return;

        FPlayer fPlayer = new FPlayer(offlinePlayer);
        fPlayerHashMap.put(uuid, fPlayer);
    }


    public static FPlayer addPlayer(@NotNull Player player) {
        String uuid = player.getUniqueId().toString();
        if (fPlayerHashMap.containsKey(uuid)) {
            FPlayer fPlayer = getPlayer(uuid);
            fPlayer.initialize(player);
            return fPlayer;
        }

        FPlayer fPlayer = new FPlayer(player);
        fPlayerHashMap.put(uuid, fPlayer);
        fPlayer.initialize(player);
        Main.getDatabase().setPlayer(fPlayer.getUUID());
        return fPlayer;
    }

    public static FPlayer getPlayerFromName(String name) {
        return fPlayerHashMap.values()
                .parallelStream()
                .filter(fPlayer -> fPlayer.getRealName().equals(name))
                .findFirst().orElse(null);
    }

    public static FPlayer getPlayer(@NotNull OfflinePlayer offlinePlayer) {
        return getPlayer(offlinePlayer.getUniqueId());
    }

    public static FPlayer getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

    public static FPlayer getPlayer(String uuid) {
        return fPlayerHashMap.get(uuid);
    }

    public static FPlayer getPlayer(UUID uuid) {
        return getPlayer(uuid.toString());
    }

    public static void removePlayer(String uuid) {
        fPlayerHashMap.remove(uuid);
    }

    public static void removePlayer(UUID uuid) {
        removePlayer(uuid.toString());
    }

    public static void removePlayer(@NotNull Player player) {
        removePlayer(player.getUniqueId());
    }

}
