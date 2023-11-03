package net.flectone.chat.module.integrations;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.chat.FlectoneChat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class FSuperVanish implements Listener, FIntegration {

    public FSuperVanish() {
        init();
    }

    @EventHandler
    public void onHide(@NotNull PlayerHideEvent event) {
        if (event.isCancelled()) return;
//        QuitListener.sendQuitMessage(event.getPlayer(), "server-message.quit");
    }

    @EventHandler
    public void onShow(@NotNull PlayerShowEvent event) {
        if (event.isCancelled()) return;
//        JoinListener.sendJoinMessage(event.getPlayer());
    }

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, FlectoneChat.getInstance());
        FlectoneChat.info("SuperVanish detected and hooked");
    }
}
