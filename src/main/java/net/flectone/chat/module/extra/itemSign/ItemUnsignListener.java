package net.flectone.chat.module.extra.itemSign;

import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.model.player.FPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;

public class ItemUnsignListener extends FListener {
    public ItemUnsignListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void unsignEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (clickedBlock.getLocation().getWorld() == null) return;

        Player player = event.getPlayer();
        if (!config.getVaultBoolean(player, getModule() + ".unsign.enable")) return;
        if (hasNoPermission(player, "unsign")) return;

        String configBlock = config.getVaultString(player, getModule() + ".unsign.block");
        if (!clickedBlock.getType().toString().equalsIgnoreCase(configBlock)) return;

        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer == null) return;

        if (fPlayer.isMuted()) {
            fPlayer.sendMutedMessage();
            return;
        }

        if (fPlayer.isHaveCooldown(getModule() + ".unsign")) {
            fPlayer.sendCDMessage("unsign");
            return;
        }

        boolean dropDyeEnabled = config.getVaultBoolean(player, getModule() + ".unsign.drop-dye");
        boolean isCompleted = ((ItemSignModule) getModule()).unsign(player, clickedBlock.getLocation(),
                player.getInventory(), dropDyeEnabled);

        event.setCancelled(isCompleted);
    }
}
