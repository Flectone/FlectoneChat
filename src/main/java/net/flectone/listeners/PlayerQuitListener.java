package net.flectone.listeners;

import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FEntity;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.config;

public class PlayerQuitListener implements Listener {

    public static void sendQuitMessage(@NotNull Player player) {
        boolean isEnable = config.getBoolean("player.quit.message.enable");
        if (!isEnable || FSuperVanish.isVanished(player)) return;

        String string = FPlayer.getVaultLocaleString(player, "player.quit.<group>.message")
                .replace("<player>", player.getName());

        FCommand fCommand = new FCommand(player, "quit", "quit", string.split(" "));

        fCommand.sendGlobalMessage(string, "", null, true);
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
