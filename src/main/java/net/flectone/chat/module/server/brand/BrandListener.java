package net.flectone.chat.module.server.brand;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class BrandListener extends FListener {
    public BrandListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void brandEvent(@NotNull PlayerJoinEvent event) {
        FModule fModule = FlectoneChat.getModuleManager().get(BrandModule.class);
        if (fModule instanceof BrandModule brandModule) {
            brandModule.setBrand(event.getPlayer(), ((BrandModule) getModule()).incrementIndexAndGet(event.getPlayer()));
        }
    }
}
