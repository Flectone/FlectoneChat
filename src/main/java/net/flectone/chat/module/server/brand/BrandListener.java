package net.flectone.chat.module.server.brand;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class BrandListener extends FListener {

    private FModuleManager moduleManager;

    public BrandListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();

        moduleManager = FlectoneChat.getPlugin().getModuleManager();
    }

    @EventHandler
    public void brandEvent(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!getModule().isEnabledFor(player)) return;
        if (hasNoPermission(player)) return;

        FModule fModule = moduleManager.get(BrandModule.class);
        if (fModule instanceof BrandModule brandModule) {
            brandModule.setBrand(event.getPlayer(), ((BrandModule) getModule()).incrementIndexAndGet(player));
        }
    }
}
