package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.entity.FEntity;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.info.ModInfo;
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

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                createFPlayer(player).synchronizeDatabase();
            }
        });

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

            ModInfo modInfo = new ModInfo(offlinePlayer.getUniqueId().toString(), -1, reason, source);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
                    Main.getDatabase().updatePlayerInfo("bans", modInfo));

            banList.pardon(offlinePlayer.getName());
        });
    }

    public static void clearPlayers() {
        onlineFPlayers.values().forEach(FEntity::removePlayerFromTeam);

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
                .findFirst()
                .orElseGet(() -> {
                    List<OfflinePlayer> offlinePlayerList = Arrays.stream(Bukkit.getOfflinePlayers())
                            .parallel()
                            .filter(player -> player.getName() != null && player.getName().equalsIgnoreCase(name))
                            .toList();

                    if (offlinePlayerList.size() == 1) return offlinePlayerList.get(0);
                    return null;
                });

        if (offlinePlayer == null) return null;

        return FPlayerManager.getPlayers().parallelStream()
                .filter(player -> player != null && player.getOfflinePlayer().equals(offlinePlayer))
                .findFirst().orElse(new FPlayer(offlinePlayer));
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
