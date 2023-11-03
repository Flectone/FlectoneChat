package net.flectone.chat.module.serverMessage.join;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class JoinListener extends FListener {
    public JoinListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoinEvent(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;
        if (IntegrationsModule.isVanished(player)) return;

        String string = player.hasPlayedBefore() || !config.getVaultBoolean(player, getModule() + ".first-time.enable")
                ? locale.getVaultString(player, getModule() + ".message")
                : locale.getVaultString(player, getModule() + ".first-time.message");

        FlectoneChat.getDatabase().execute(() -> ((JoinModule) getModule()).sendAll(player, string));

        event.setJoinMessage(null);
    }
}
