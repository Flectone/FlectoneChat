package net.flectone.integrations.supervanish;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.listeners.PlayerJoinListener;
import net.flectone.listeners.PlayerQuitListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;

public class FSuperVanish implements Listener {

    public static boolean isVanished(Player player) {
        return player.getMetadata("vanished").parallelStream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @EventHandler
    public void onHide(PlayerHideEvent e) {
        if(e.isCancelled()) return;
        PlayerQuitListener.sendQuitMessage(e.getPlayer());
    }

    @EventHandler
    public void onShow(PlayerShowEvent e) {
        if(e.isCancelled()) return;
        PlayerJoinListener.sendJoinMessage(e.getPlayer());
    }

}
