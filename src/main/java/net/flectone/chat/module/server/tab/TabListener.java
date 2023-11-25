package net.flectone.chat.module.server.tab;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class TabListener extends FListener {
    public TabListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void playerJoinEvent(@NotNull PlayerJoinEvent event) {
        if (!getModule().isEnabledFor(event.getPlayer())) return;

        ((TabModule) getModule()).update(event.getPlayer());
    }
}
