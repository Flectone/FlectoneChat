package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;

public class PlayerGlassKnockingListener implements Listener {

    private static final List<String> GLASSES = new ArrayList<>();

    static {
        GLASSES.add(Material.GLASS.name());
        GLASSES.add(Material.BLACK_STAINED_GLASS.name());
        GLASSES.add(Material.BLUE_STAINED_GLASS.name());
        GLASSES.add(Material.BROWN_STAINED_GLASS.name());
        GLASSES.add(Material.CYAN_STAINED_GLASS.name());
        GLASSES.add(Material.GRAY_STAINED_GLASS.name());
        GLASSES.add(Material.GREEN_STAINED_GLASS.name());
        GLASSES.add(Material.LIGHT_BLUE_STAINED_GLASS.name());
        GLASSES.add(Material.LIGHT_GRAY_STAINED_GLASS.name());
        GLASSES.add(Material.LIME_STAINED_GLASS.name());
        GLASSES.add(Material.MAGENTA_STAINED_GLASS.name());
        GLASSES.add(Material.ORANGE_STAINED_GLASS.name());
        GLASSES.add(Material.PINK_STAINED_GLASS.name());
        GLASSES.add(Material.PURPLE_STAINED_GLASS.name());
        GLASSES.add(Material.RED_STAINED_GLASS.name());
        GLASSES.add(Material.WHITE_STAINED_GLASS.name());
        GLASSES.add(Material.YELLOW_STAINED_GLASS.name());
        GLASSES.add(Material.GLASS_PANE.name());
        GLASSES.add(Material.BLACK_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.BLUE_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.BROWN_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.CYAN_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.GRAY_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.GREEN_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.LIME_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.MAGENTA_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.ORANGE_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.PINK_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.PURPLE_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.RED_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.WHITE_STAINED_GLASS_PANE.name());
        GLASSES.add(Material.YELLOW_STAINED_GLASS_PANE.name());
        GLASSES.add("TINTED_GLASS");
    }

    @EventHandler
    public void onGlassKnockingEvent(PlayerInteractEvent event) {
        if (!config.getBoolean("player.glass-knocking.enable")) return;
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (!player.isSneaking()
                || block == null
                || !(GLASSES.contains(block.getType().name()))) return;

        ObjectUtil.playSound(player, block.getLocation(), "glass-knocking");
    }

}
