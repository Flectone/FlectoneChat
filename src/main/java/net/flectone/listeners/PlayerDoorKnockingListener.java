package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static net.flectone.managers.FileManager.config;

public class PlayerDoorKnockingListener implements Listener {

    @EventHandler
    public void onDoorKnockingEvent(PlayerInteractEvent event) {
        if (!config.getBoolean("player.door-knocking.enable")) return;
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (!player.isSneaking()
                || block == null
                || !(block.getBlockData() instanceof Door)) return;

        ObjectUtil.playSound(player, block.getLocation(),"door-knocking");
    }

}
