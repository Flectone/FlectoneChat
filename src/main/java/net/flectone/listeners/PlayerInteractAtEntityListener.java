package net.flectone.listeners;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.config;

public class PlayerInteractAtEntityListener implements Listener {

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player player)) return;

        if (config.getBoolean("player.name-visible")) return;

        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if (fPlayer == null) return;

        String formatMessage = FPlayer.getVaultLocaleString(player, "player.right-click.<group>.message")
                .replace("<player>", fPlayer.getDisplayName());
        formatMessage = ObjectUtil.formatString(formatMessage, event.getPlayer(), player);

        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, FComponent.fromLegacyText(formatMessage));

    }
}
