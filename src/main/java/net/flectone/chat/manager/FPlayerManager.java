package net.flectone.chat.manager;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


@Getter
public class FPlayerManager {

    private final ArrayList<UUID> bannedPlayers = new ArrayList<>();
    private final ArrayList<String> mutedPlayers = new ArrayList<>();
    private final HashMap<String, List<Moderation>> warnsPlayers = new HashMap<>();
    private final ArrayList<String> offlinePlayers = new ArrayList<>();
    private final HashMap<String, FPlayer> playerHashMap = new HashMap<>();

    public void loadOfflinePlayers() {
        offlinePlayers.addAll(Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName).toList());
    }

    // ONLY FOR TABCOMPLETE /unwarn /unmute /unban /warnlist because TABCOMPLETE is sync method (spigot)
    public void loadTabCompleteData() {
        FlectoneChat.getPlugin().getDatabase().execute(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `player` FROM `bans` WHERE `time`>? OR `time`=-1");
            preparedStatement.setInt(1, TimeUtil.getCurrentTime());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String playerUUID = resultSet.getString("player");
                if (playerUUID == null) continue;

                bannedPlayers.add(UUID.fromString(playerUUID));
            }

            preparedStatement = connection.prepareStatement("SELECT `player` FROM `mutes` WHERE `time`>?");
            preparedStatement.setInt(1, TimeUtil.getCurrentTime());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String playerUUID = resultSet.getString("player");
                if (playerUUID == null) continue;

                mutedPlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName());
            }

            preparedStatement = connection.prepareStatement("SELECT * FROM `warns` WHERE `time`>?");
            preparedStatement.setInt(1, TimeUtil.getCurrentTime());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String playerUUID = resultSet.getString("player");
                if (playerUUID == null) continue;

                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
                List<Moderation> warns = warnsPlayers.get(playerName);
                if (warns == null) warns = new ArrayList<>();

                int id = resultSet.getInt("id");
                int time = resultSet.getInt("time");
                String reason = resultSet.getString("reason");
                String moderator = resultSet.getString("moderator");
                warns.add(new Moderation(id, playerUUID, time, reason, moderator, Moderation.Type.WARN));

                warnsPlayers.put(playerName, warns);
            }
        });
    }

    public void add(FPlayer fPlayer) {
        playerHashMap.put(fPlayer.getMinecraftName(), fPlayer);
    }

    public void remove(@NotNull FPlayer fPlayer) {
        playerHashMap.remove(fPlayer.getMinecraftName());
    }

    @Nullable
    public FPlayer get(@Nullable String name) {
        if (name == null) return null;

        FPlayer fPlayer = playerHashMap.get(name);
        if (fPlayer != null) return fPlayer;

        return playerHashMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    @Nullable
    public FPlayer get(@Nullable UUID uuid) {
        if (uuid == null) return null;

        return playerHashMap.values()
                .stream()
                .filter(Objects::nonNull)
                .filter(fPlayer -> fPlayer.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public FPlayer getOffline(@Nullable String name) {
        FPlayer fPlayer = get(name);
        if (fPlayer != null) return fPlayer;

        List<OfflinePlayer> offlinePlayerList = Arrays.asList(Bukkit.getOfflinePlayers());

        offlinePlayerList = offlinePlayerList
                .parallelStream()
                .filter(player -> player.getName() != null && player.getName().equalsIgnoreCase(name))
                .toList();

        if (offlinePlayerList.size() > 1) {
            FlectoneChat.warning("Please remove offline players with the same names, it can cause problems");
        }

        OfflinePlayer offlinePlayer = offlinePlayerList.stream()
                .filter(player -> Objects.equals(player.getName(), name))
                .findFirst()
                .orElse(null);

        fPlayer = offlinePlayer != null
                ? new FPlayer(offlinePlayer)
                : null;

        playerHashMap.put(name, fPlayer);
        return fPlayer;
    }

    @Nullable
    public FPlayer get(@NotNull Player player) {
        return get(player.getName());
    }

    public void loadOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player ->
                new FPlayer(player).init());
    }

    public void terminateAll() {
        playerHashMap.values().forEach(fPlayer -> {
            if (fPlayer == null) return;
            fPlayer.toDatabase();
            fPlayer.unregisterTeam();
        });

        playerHashMap.clear();
        offlinePlayers.clear();
        bannedPlayers.clear();
        mutedPlayers.clear();
        warnsPlayers.clear();
    }
}
