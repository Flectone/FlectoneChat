package net.flectone.chat.module.serverMessage.quit;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class QuitListener extends FListener {

    public QuitListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;
        if (IntegrationsModule.isVanished(player)) return;

        boolean hasPlayerBefore = player.hasPlayedBefore() || !config.getVaultBoolean(player, getModule() + ".first-time.enable");

        String string = hasPlayerBefore
                ? locale.getVaultString(player, getModule() + ".message")
                : locale.getVaultString(player, getModule() + ".first-time.message");

        ((QuitModule) getModule()).sendAll(player, string);

        IntegrationsModule.sendDiscordQuit(player, hasPlayerBefore ? "usually" : "first");

        event.setQuitMessage(null);
    }
}
