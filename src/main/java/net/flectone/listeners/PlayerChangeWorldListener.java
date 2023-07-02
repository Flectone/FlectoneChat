package net.flectone.listeners;

import net.flectone.managers.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangeWorldListener implements Listener {

    @EventHandler
    public void changeWorldEvent(PlayerChangedWorldEvent event){
        PlayerManager.getPlayer(event.getPlayer()).setName(event.getPlayer().getWorld());
    }
}
