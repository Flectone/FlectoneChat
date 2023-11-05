package net.flectone.chat.util;

import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.flectone.chat.manager.FileManager.config;

public class PlayerUtil {

    public static String getIP(Player player) {
        try {
            InetSocketAddress playerAddress = player.getAddress();
            if (playerAddress == null || playerAddress.isUnresolved()) return "0.0.0.0";
            return playerAddress.getHostString();
        } catch (Throwable e) {
            return "0.0.0.0";
        }
    }

    public static String getPing(Player player) {
        return String.valueOf(player.getPing());
    }

    @NotNull
    public static String getPrefix(@NotNull Player player) {
        String prefix = IntegrationsModule.getPrefix(player);
        return prefix != null ? prefix : "";
    }

    @NotNull
    public static String getSuffix(@NotNull Player player) {
        String suffix = IntegrationsModule.getSuffix(player);
        return suffix != null ? suffix : "";
    }

    @NotNull
    public static String getPrimaryGroup(@Nullable CommandSender sender) {
        String playerGroup = null;

        if (sender instanceof Player player) {
            playerGroup = IntegrationsModule.getPrimaryGroup(player);
        }

        return playerGroup != null ? playerGroup : "default";
    }

    @NotNull
    public static String generateSortString(int rank, String playerName) {
        String paddedRank = String.format("%010d", Integer.MAX_VALUE - rank);
        String paddedName = String.format("%-16s", playerName);
        return paddedRank + paddedName;
    }

    @NotNull
    public static Collection<? extends Player> getPlayersWithFeature(@NotNull String path) {
        return getPlayersWithFeature(Bukkit.getOnlinePlayers(), path);
    }

    @NotNull
    public static Collection<? extends Player> getPlayersWithFeature(@NotNull Collection<? extends Player> playerCollection, @NotNull String path) {
        return playerCollection.stream()
                .filter(player -> config.getVaultBoolean(player, path))
                .collect(Collectors.toList());
    }
}
