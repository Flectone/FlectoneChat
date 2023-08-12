package net.flectone.integrations.supervanish;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.listeners.PlayerJoinListener;
import net.flectone.listeners.PlayerQuitListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

public class FSuperVanish implements Listener, HookInterface {

    public static boolean isVanished(@NotNull Player player) {
        return player.getMetadata("vanished").parallelStream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @EventHandler
    public void onHide(@NotNull PlayerHideEvent event) {
        if (event.isCancelled()) return;
        PlayerQuitListener.sendQuitMessage(event.getPlayer());
    }

    @EventHandler
    public void onShow(@NotNull PlayerShowEvent event) {
        if (event.isCancelled()) return;
        PlayerJoinListener.sendJoinMessage(event.getPlayer());
    }

    @Override
    public void hook() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        Main.info("\uD83D\uDD12 SuperVanish detected and hooked");
    }
}
