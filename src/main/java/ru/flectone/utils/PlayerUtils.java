package ru.flectone.utils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import ru.flectone.custom.FPlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerUtils {

    public static HashMap<UUID, FPlayer> onlinePlayers;

    public static void addPlayer(FPlayer fplayer) {
        onlinePlayers.put(fplayer.getUUID(), fplayer);
    }

    public static FPlayer getPlayer(UUID uuid){
        return onlinePlayers.get(uuid);
    }

    public static FPlayer getPlayer(HumanEntity player){
        return onlinePlayers.get(player.getUniqueId());
    }

    public static void setOnlinePlayers(HashMap<UUID, FPlayer> onlinePlayers) {
        PlayerUtils.onlinePlayers = onlinePlayers;
    }

    public static void removePlayer(Player player){
        onlinePlayers.remove(player.getUniqueId());
    }

    public static void removePlayer(UUID uuid){
        onlinePlayers.remove(uuid);
    }
}
