package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.player.PlayerMod;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class FPlayerManager {

    private static final List<String> bannedPlayers = new ArrayList<>();
    private static final HashMap<String, FPlayer> usedFPlayers = new HashMap<>();
    private static Scoreboard scoreBoard;

    public static void setScoreBoard() {
        scoreBoard = config.getBoolean("scoreboard.custom")
                ? Bukkit.getScoreboardManager().getNewScoreboard()
                : Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @NotNull
    public static Scoreboard getScoreBoard() {
        return scoreBoard;
    }

    public static void loadPlayers() {
        Main.getDataThreadPool().execute(() -> {

            Arrays.stream(Bukkit.getOfflinePlayers()).forEach(offlinePlayer -> {
                Main.getDatabase().insertPlayer(offlinePlayer.getUniqueId());

                if (offlinePlayer.getName() != null) FTabCompleter.offlinePlayerList.add(offlinePlayer.getName());
            });

            Bukkit.getOnlinePlayers().forEach(player ->
                    createFPlayer(player).synchronizeDatabase());

            Main.getDatabase().clearOldRows("mutes");
            Main.getDatabase().clearOldRows("bans");
            Main.getDatabase().clearOldRows("warns");
        });
    }

    public static List<String> getBannedPlayers() {
        return bannedPlayers;
    }

    public static void loadBanList() {
        if (!config.getBoolean("command.tempban.enable")) return;

        bannedPlayers.clear();
        Main.getDataThreadPool().execute(() ->
                bannedPlayers.addAll(Main.getDatabase().getPlayerNameList("bans", "player")));

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (banList.getBanEntries().isEmpty()) return;

        Bukkit.getBannedPlayers().parallelStream().forEach(offlinePlayer -> {
            if (offlinePlayer.getName() == null) return;

            BanEntry banEntry = banList.getBanEntry(offlinePlayer.getName());
            if (banEntry == null) return;

            String source = banEntry.getSource();

            source = source.equalsIgnoreCase("console") || source.equalsIgnoreCase("plugin")
                    ? null
                    : Bukkit.getOfflinePlayer(source).getUniqueId().toString();

            String reason = banEntry.getReason() != null
                    ? banEntry.getReason()
                    : locale.getString("command.tempban.default-reason");

            PlayerMod playerMod = new PlayerMod(offlinePlayer.getUniqueId().toString(), -1, reason, source);
            Main.getDataThreadPool().execute(() ->
                    Main.getDatabase().updatePlayerInfo("bans", playerMod));

            banList.pardon(offlinePlayer.getName());
        });
    }

    public static void clearPlayers() {
        usedFPlayers.values().stream()
                .filter(Objects::nonNull)
                .forEach(FPlayer::removeTeam);

        usedFPlayers.clear();
    }

    public static FPlayer createFPlayer(@NotNull Player player) {
        FPlayer fPlayer = new FPlayer(player);
        usedFPlayers.put(player.getName(), fPlayer);

        fPlayer.initialize(player);
        return fPlayer;
    }

    @Nullable
    public static FPlayer getPlayerFromName(@NotNull String name) {
        if (usedFPlayers.containsKey(name)) return usedFPlayers.get(name);

        List<OfflinePlayer> offlinePlayerList = Arrays.asList(Bukkit.getOfflinePlayers());

        offlinePlayerList = offlinePlayerList
                .parallelStream()
                .filter(player -> player.getName() != null && player.getName().equalsIgnoreCase(name))
                .toList();

        OfflinePlayer offlinePlayer = offlinePlayerList.size() == 1
                ? offlinePlayerList.get(0)
                : offlinePlayerList.stream()
                .filter(player -> player.getName().equals(name))
                .findFirst().orElse(null);

        if (offlinePlayer == null) {
            usedFPlayers.put(name, null);
            return null;
        }

        FPlayer fPlayer = FPlayerManager.getPlayer(offlinePlayer);
        fPlayer = fPlayer != null ? fPlayer : new FPlayer(offlinePlayer);
        usedFPlayers.put(name, fPlayer);

        return fPlayer;
    }

    public static HashMap<String, FPlayer> getUsedFPlayers() {
        return usedFPlayers;
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull OfflinePlayer offlinePlayer) {
        if (offlinePlayer.getName() == null) return null;
        return getPlayer(offlinePlayer.getName());
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull Player player) {
        return getPlayer(player.getName());
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull String name) {
        return usedFPlayers.get(name);
    }

    @Nullable
    public static FPlayer getPlayer(@NotNull UUID uuid) {
        return usedFPlayers.get(Bukkit.getOfflinePlayer(uuid).getName());
    }


    public static void removePlayer(@NotNull String name) {
        if (usedFPlayers.get(name) == null) return;
        usedFPlayers.get(name).removeTeam();
        usedFPlayers.remove(name);
    }

    public static void removePlayer(@NotNull Player player) {
        removePlayer(player.getName());
    }

}
