package net.flectone.chat.util;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.stream.Collectors;

public class PlayerUtil {

    private static final String WEBSITE_AVATAR_URL = "https://mc-heads.net/avatar/<player>/8.png";

    public static String getIP(Player player) {
        try {
            InetSocketAddress playerAddress = player.getAddress();
            if (playerAddress == null || playerAddress.isUnresolved()) return "0.0.0.0";
            return playerAddress.getHostString();
        } catch (Throwable e) {
            return "0.0.0.0";
        }
    }

    public static int getObjectiveScore(@NotNull Player player, @NotNull String mode) {
        return switch (mode.toLowerCase()) {
            case "health" -> (int) Math.round(player.getHealth() * 10.0)/10;
            case "level" -> player.getLevel();
            case "food" -> player.getFoodLevel();
            case "ping" -> player.getPing();
            case "armor" -> {
                AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
                yield armor != null ? (int) Math.round(armor.getValue() * 10.0)/10 : 0;
            }
            case "attack" -> {
                AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                yield damage != null ? (int) Math.round(damage.getValue() * 10.0)/10 : 0;
            }
            default -> 0;
        };
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
        FConfiguration config = FlectoneChat.getPlugin().getFileManager().getConfig();

        return playerCollection.stream()
                .filter(player -> config.getVaultBoolean(player, path))
                .collect(Collectors.toList());
    }

    @NotNull
    public static String constructAvatarUrl(@NotNull Player player) {
        String replacement = IntegrationsModule.getTextureId(player);
        if (replacement == null) replacement = player.getUniqueId().toString();

        return WEBSITE_AVATAR_URL.replace("<player>", replacement);
    }
}
