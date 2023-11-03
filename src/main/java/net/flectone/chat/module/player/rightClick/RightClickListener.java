package net.flectone.chat.module.player.rightClick;

import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;

public class RightClickListener extends FListener {

    public RightClickListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void rightClickEvent(@NotNull PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player player)) return;
        if (hasNoPermission(player)) return;

        String formatMessage = config.getVaultString(player, getModule() + ".format");

        formatMessage = MessageUtil.formatAll(player, event.getPlayer(), MessageUtil.formatPlayerString(player, formatMessage));

        ((RightClickModule) getModule()).sendAction(event.getPlayer(), formatMessage);
    }
}