package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FEntity;
import net.flectone.integrations.supervanish.FSuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {

    public static void sendQuitMessage(@NotNull Player player) {
        boolean isEnable = Main.config.getBoolean("player.quit.message.enable");
        if (!isEnable || FSuperVanish.isVanished(player)) return;

        FCommand fCommand = new FCommand(player, "quit", "quit", new String[]{});

        String string = Main.locale.getString("player.quit.message")
                .replace("<player>", player.getName());

        fCommand.sendGlobalMessage(string);
    }

    @EventHandler
    public void leftPlayer(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        event.setQuitMessage(null);
        sendQuitMessage(player);
    }
}
