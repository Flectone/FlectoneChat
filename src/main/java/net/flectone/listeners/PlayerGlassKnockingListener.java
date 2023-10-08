package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static net.flectone.managers.FileManager.config;

public class PlayerGlassKnockingListener implements Listener {

    @EventHandler
    public void onGlassKnockingEvent(PlayerInteractEvent event) {
        if (!config.getBoolean("player.glass-knocking.enable")) return;
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (!player.isSneaking()
                || block == null
                || !(block.getType().toString().contains("GLASS"))) return;

        ObjectUtil.playSound(player, block.getLocation(), "glass-knocking");
    }

}
