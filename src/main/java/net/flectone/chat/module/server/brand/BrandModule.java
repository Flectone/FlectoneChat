package net.flectone.chat.module.server.brand;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static net.flectone.chat.manager.FileManager.locale;

public class BrandModule extends FModule {

    private static final HashMap<Player, Integer> PLAYER_INDEX_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> GROUP_BRAND_LIST = new HashMap<>();

    private static Field playerChannelsField;
    private static String channel;

    public BrandModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        try {
            Class.forName("org.bukkit.entity.Dolphin");
            channel = "minecraft:brand";
        } catch (ClassNotFoundException ignored) {
            channel = "MC|Brand";
        }

        try {
            Method registerMethod = FlectoneChat.getInstance().getServer().getMessenger().getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
            registerMethod.setAccessible(true);
            registerMethod.invoke(FlectoneChat.getInstance().getServer().getMessenger(), FlectoneChat.getInstance(), channel);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error while attempting to register plugin message channel", e);
        }

        FActionManager.add(new BrandListener(this));
        FActionManager.add(new BrandTicker(this));
    }

    public String incrementIndexAndGet(Player player) {
        String playerGroup = PlayerUtil.getPrimaryGroup(player);
        List<String> brandList = GROUP_BRAND_LIST.get(playerGroup);
        if (brandList == null) {
            brandList = locale.getVaultStringList(player, this + ".message");
            GROUP_BRAND_LIST.put(playerGroup, brandList);
        }

        Integer index = PLAYER_INDEX_MAP.get(player);
        if (index == null) index = 0;

        index++;
        index = index % brandList.size();
        PLAYER_INDEX_MAP.put(player, index);

        return MessageUtil.formatAll(player, brandList.get(index));
    }

    public void setBrand(@NotNull Player player, @NotNull String message) {
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

        updateBrand(player, message);
    }

    public void updateBrand(@NotNull Player player, @NotNull String brandString) {
        if (hasNoPermission(player)) return;
        player.sendPluginMessage(FlectoneChat.getInstance(), channel,
                new PacketSerializer(brandString + ChatColor.RESET).toArray());
    }
}
