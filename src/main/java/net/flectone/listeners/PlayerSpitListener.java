package net.flectone.listeners;

import net.flectone.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static net.flectone.managers.FileManager.config;

public class PlayerSpitListener implements Listener {

    @EventHandler
    public void onPlayerSpitEvent(PlayerInteractEvent event) {
        if (!config.getBoolean("command.spit.enable")) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        ItemStack item = event.getItem();

        if (item == null) return;

        Material configMaterial;

        try {
            configMaterial = Material.valueOf(config.getString("command.spit.item").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            Main.warning("Item for spit was not found");
            configMaterial = Material.WHITE_DYE;
        }

        if (!item.getType().equals(configMaterial)) return;

        Bukkit.dispatchCommand(event.getPlayer(), "spit");

    }
}
