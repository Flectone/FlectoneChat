package net.flectone.chat.module.serverMessage.quit;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class QuitListener extends FListener {
    public QuitListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void playerQuitEvent(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;
        if (IntegrationsModule.isVanished(player)) return;

        String string = player.hasPlayedBefore() || !config.getVaultBoolean(player, getModule() + ".first-time.enable")
                ? locale.getVaultString(player, getModule() + ".message")
                : locale.getVaultString(player, getModule() + ".first-time.message");

        ((QuitModule) getModule()).sendAll(player, string);

        event.setQuitMessage(null);
    }
}
