package net.flectone.listeners;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChangeWorldListener implements Listener {

    @EventHandler
    public void changeWorldEvent(@NotNull PlayerChangedWorldEvent event) {
        FPlayer fPlayer = FPlayerManager.getPlayer(event.getPlayer());
        if (fPlayer == null) return;

        fPlayer.setWorldPrefix(event.getPlayer().getWorld());
    }
}
