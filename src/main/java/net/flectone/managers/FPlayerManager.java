package net.flectone.managers;

import net.flectone.Main;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FPlayerManager {

    private static final HashMap<String, FPlayer> fPlayerHashMap = new HashMap<>();

    public static Collection<FPlayer> getPlayers(){
        return fPlayerHashMap.values();
    }

    public static void loadPlayers(){
        Arrays.stream(Bukkit.getOfflinePlayers())
                .forEach(FPlayerManager::addPlayer);

        Main.getDatabase().loadDatabase();

        Bukkit.getOnlinePlayers().forEach(player -> getPlayer(player).initialize(player));
    }

    public static void uploadPlayers(){
        fPlayerHashMap.values()
                .stream()
                .filter(FPlayer::isUpdated)
                .forEach(fPlayer -> {
                    Main.getDatabase().uploadDatabase(fPlayer);
                    fPlayer.setUpdated(false);
                });
    }

    public static void removePlayersFromTeams(){
        fPlayerHashMap.values().forEach(FEntity::removePlayerFromTeam);
    }

    public static void addPlayer(@NotNull OfflinePlayer offlinePlayer){
        String uuid = offlinePlayer.getUniqueId().toString();
        if(fPlayerHashMap.containsKey(uuid)) return;

        FPlayer fPlayer = new FPlayer(offlinePlayer);
        fPlayerHashMap.put(uuid, fPlayer);
    }

    public static FPlayer addPlayer(@NotNull Player player){
        String uuid = player.getUniqueId().toString();
        if(fPlayerHashMap.containsKey(uuid)) {
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

    public static void addPlayer(String uuid){
        addPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
    }

    public static void addPlayer(UUID uuid){
        addPlayer(Bukkit.getOfflinePlayer(uuid));
    }

    public static FPlayer getPlayerFromName(String name){
        FPlayer player = null;
        player = fPlayerHashMap.values()
                .stream()
                .filter(fPlayer -> fPlayer.getRealName().equals(name))
                .findFirst().orElse(null);

        return player;
    }

    public static FPlayer getPlayer(@NotNull OfflinePlayer offlinePlayer){
        return getPlayer(offlinePlayer.getUniqueId());
    }

    public static FPlayer getPlayer(@NotNull Player player){
        return getPlayer(player.getUniqueId());
    }

    public static FPlayer getPlayer(String uuid){
        return fPlayerHashMap.get(uuid);
    }

    public static FPlayer getPlayer(UUID uuid){
        return getPlayer(uuid.toString());
    }

    public static void removePlayer(String uuid){
        fPlayerHashMap.remove(uuid);
    }

    public static void removePlayer(UUID uuid){
        removePlayer(uuid.toString());
    }

    public static void removePlayer(@NotNull Player player){
        removePlayer(player.getUniqueId());
    }

    public static void removePlayer(@NotNull OfflinePlayer offlinePlayer){
        removePlayer(offlinePlayer.getUniqueId());
    }
}
