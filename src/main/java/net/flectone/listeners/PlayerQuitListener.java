package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void leftPlayer(PlayerQuitEvent event){
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        event.setQuitMessage(null);
        FCommands fCommands = new FCommands(player, "quit", "quit", new String[]{});

        String string = Main.locale.getString("left.message")
                .replace("<player>", player.getName());
        fCommands.sendGlobalMessage(string);

//        FPlayerManager.removePlayer(player);
    }
}
