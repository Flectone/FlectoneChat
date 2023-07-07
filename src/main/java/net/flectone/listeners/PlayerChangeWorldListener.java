package net.flectone.listeners;

import net.flectone.managers.FPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    @EventHandler
    public void changeWorldEvent(PlayerChangedWorldEvent event){
        FPlayerManager.getPlayer(event.getPlayer()).setWorldPrefix(event.getPlayer().getWorld());
    }
}
