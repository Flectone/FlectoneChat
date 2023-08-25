package net.flectone.listeners;

import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class PlayerQuitListener implements Listener {

    public static void sendQuitMessage(@NotNull Player player) {
        boolean isEnable = config.getBoolean("player.quit.message.enable");
        if (!isEnable || FSuperVanish.isVanished(player)) return;

        FCommand fCommand = new FCommand(player, "quit", "quit", new String[]{});

        String string = locale.getString("player.quit.message")
                .replace("<player>", player.getName());

        fCommand.sendGlobalMessage(string);
    }

    @EventHandler
    public void leftPlayer(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        event.setQuitMessage(null);
        sendQuitMessage(player);

        FPlayerManager.removePlayer(event.getPlayer());
    }
}
