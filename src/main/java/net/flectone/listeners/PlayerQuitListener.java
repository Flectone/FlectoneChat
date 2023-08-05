package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FEntity;
import net.flectone.integrations.supervanish.FSuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void leftPlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        event.setQuitMessage(null);
        sendQuitMessage(player);
    }

    public static void sendQuitMessage(Player player){
        boolean isEnable = Main.config.getBoolean("player.quit.message.enable");
        if (!isEnable || FSuperVanish.isVanished(player)) return;

        FCommands fCommands = new FCommands(player, "quit", "quit", new String[]{});

        String string = Main.locale.getString("player.quit.message")
                .replace("<player>", player.getName());

        fCommands.sendGlobalMessage(string);
    }
}
