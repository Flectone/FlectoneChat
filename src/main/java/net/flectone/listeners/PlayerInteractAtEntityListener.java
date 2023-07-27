package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractAtEntityListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;

        if (Main.config.getBoolean("player.name-visible")) return;

        String formatMessage = Main.locale.getFormatString("player.right-click-message", event.getPlayer())
                .replace("<player>", FPlayerManager.getPlayer((Player) event.getRightClicked()).getDisplayName());

        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatMessage));

    }
}
