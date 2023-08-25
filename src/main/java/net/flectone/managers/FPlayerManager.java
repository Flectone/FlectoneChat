package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.entity.DatabasePlayer;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class FPlayerManager {

    private static final HashMap<UUID, FPlayer> onlineFPlayers = new HashMap<>();
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
        return onlineFPlayers.values();
    }

    public static void loadPlayers() {

        for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            Main.getDatabase().insertPlayer(offlinePlayer.getUniqueId());
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            createFPlayer(player);
        }
    }

    public static void loadBanList() {
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

            DatabasePlayer databasePlayer = new DatabasePlayer(offlinePlayer.getUniqueId().toString(), -1, reason, source);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                    Main.getDatabase().saveModeratorAction("bans", databasePlayer));

            banList.pardon(offlinePlayer.getName());
        });
    }

    public static void clearPlayers() {
        onlineFPlayers.values().forEach(fPlayer -> {
            FEntity.removePlayerFromTeam(fPlayer);
        });

        onlineFPlayers.clear();
    }

    public static FPlayer createFPlayer(@NotNull Player player) {
        FPlayer fPlayer = new FPlayer(player);
        onlineFPlayers.put(player.getUniqueId(), fPlayer);

        fPlayer.initialize(player);
        return fPlayer;
    }

    @Nullable
    public static FPlayer getPlayerFromName(@NotNull String name) {
        OfflinePlayer offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .parallel()
                .filter(player -> player.getName() != null && player.getName().equals(name))
                .findFirst().orElse(null);

        return offlinePlayer != null
                ? FPlayerManager.getPlayers().parallelStream()
                    .filter(player -> player != null && player.getOfflinePlayer().equals(offlinePlayer))
                    .findFirst().orElse(new FPlayer(offlinePlayer))
                : null;
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
        return onlineFPlayers.get(uuid);
    }


    public static void removePlayer(@NotNull UUID uuid) {
        onlineFPlayers.remove(uuid);
    }

    public static void removePlayer(@NotNull Player player) {
        removePlayer(player.getUniqueId());
    }

}
