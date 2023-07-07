package net.flectone.listeners;

import net.flectone.commands.CommandAfk;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    @EventHandler
    public void checkPlayerUseCommand(PlayerCommandPreprocessEvent event){
        FPlayer fPlayer = FPlayerManager.getPlayer(event.getPlayer());

        if(!fPlayer.isAfk() || event.getMessage().startsWith("/afk")) return;

        CommandAfk.setAfkFalse(event.getPlayer());
    }
}
