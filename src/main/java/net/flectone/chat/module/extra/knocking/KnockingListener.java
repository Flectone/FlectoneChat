package net.flectone.chat.module.extra.knocking;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class KnockingListener extends FListener {

    public KnockingListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerKnockingEvent(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (!player.isSneaking()) return;
        if (!config.getVaultBoolean(player, getModule() + ".enable")) return;
        if (!getModule().isEnabledFor(player)) return;
        if (hasNoPermission(player)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        String blockType = block.getType().toString().toLowerCase();

        List<String> knockingTypes = config.getVaultStringList(player, getModule() + ".list");
        Optional<String> knockingBlock = knockingTypes.stream().filter(blockType::contains).findFirst();
        if (knockingBlock.isEmpty()) return;
        if (hasNoPermission(player, knockingBlock.get())) return;

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return;

        if (fPlayer.isMuted()) {
            fPlayer.sendMutedMessage();
            return;
        }

        if (fPlayer.isHaveCooldown(getModule() + "." + knockingBlock.get())) {
            fPlayer.sendCDMessage("knocking_" + knockingBlock.get());
            return;
        }

        Location location = block.getLocation();
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);

        ((KnockingModule) getModule()).knock(player, location, getModule() + "." + knockingBlock.get());
    }
}
