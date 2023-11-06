package net.flectone.chat.module.player.afkTimeout;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class AfkTimeoutListener extends FListener {
    public AfkTimeoutListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerChatEvent(@NotNull AsyncPlayerChatEvent event) {
        ((AfkTimeoutModule) getModule()).setAfk(event.getPlayer(), false, "chat");
    }

    @EventHandler
    public void playerCommandSendEvent(@NotNull PlayerCommandPreprocessEvent event) {
        if (event.getMessage().contains("/afk")) return;
        ((AfkTimeoutModule) getModule()).setAfk(event.getPlayer(), false, "commands");
    }

    @EventHandler
    public void playerClickEvent(@NotNull PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK ->
                    ((AfkTimeoutModule) getModule()).setAfk(event.getPlayer(), false, "left-click");
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK ->
                    ((AfkTimeoutModule) getModule()).setAfk(event.getPlayer(), false, "right-click");
        }
    }
}
