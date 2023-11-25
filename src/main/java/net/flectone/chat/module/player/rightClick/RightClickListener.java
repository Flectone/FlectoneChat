package net.flectone.chat.module.player.rightClick;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

public class RightClickListener extends FListener {

    public RightClickListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void rightClickEvent(@NotNull PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getRightClicked() instanceof Player player)) return;
        if (!getModule().isEnabledFor(player)) return;
        if (hasNoPermission(player)) return;
        if (!config.getVaultBoolean(player, getModule() + ".enable")) return;

        String formatMessage = config.getVaultString(player, getModule() + ".format");

        formatMessage = MessageUtil.formatAll(player, event.getPlayer(), MessageUtil.formatPlayerString(player, formatMessage));

        ((RightClickModule) getModule()).sendAction(event.getPlayer(), formatMessage);
    }
}
