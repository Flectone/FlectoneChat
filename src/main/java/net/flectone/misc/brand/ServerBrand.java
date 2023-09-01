package net.flectone.misc.brand;

import net.flectone.Main;
import net.flectone.managers.FileManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class ServerBrand {

    private static ServerBrand instance;
    private static Field playerChannelsField;
    private String channel;

    public ServerBrand() {
        instance = this;

        try {
            Class.forName("org.bukkit.entity.Dolphin");
            channel = "minecraft:brand";
        } catch (ClassNotFoundException ignored) {
            channel = "MC|Brand";
        }

        try {
            Method registerMethod = Main.getInstance().getServer().getMessenger().getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
            registerMethod.setAccessible(true);
            registerMethod.invoke(Main.getInstance().getServer().getMessenger(), Main.getInstance(), channel);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error while attempting to register plugin message channel", e);
        }

        updateEveryBrand();
    }

    public static ServerBrand getInstance() {
        if (instance == null) new ServerBrand();
        return instance;
    }

    public void setBrand(@NotNull Player player) {
        if (playerChannelsField == null) {
            try {
                playerChannelsField = player.getClass().getDeclaredField("channels");
                playerChannelsField.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        try {
            Set<String> channels = (Set<String>) playerChannelsField.get(player);
            channels.add(channel);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        updateBrand(player);
    }

    public void updateBrand(@NotNull Player player) {
        String brandString = FileManager.locale.getStringList("server.brand.message").get(0);
        updateBrand(player, brandString);
    }

    public void updateBrand(@NotNull Player player, @NotNull String brandString) {
        player.sendPluginMessage(Main.getInstance(), channel,
                new PacketSerializer(ObjectUtil.formatString(brandString, player) + ChatColor.RESET).toArray());
    }

    public void updateEveryBrand() {
        Bukkit.getOnlinePlayers().forEach(this::updateBrand);
    }
}
