package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.commands.CommandMark;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.flectone.managers.FileManager.config;

public class PlayerItemMarkListener implements Listener {
    @EventHandler
    public void onItemMarkEvent(@NotNull PlayerInteractEvent event) {

        FPlayer fPlayer = FPlayerManager.getPlayer(event.getPlayer());
        if (fPlayer == null) return;

        if (fPlayer.isAfk()) {
            CommandAfk.setAfkFalse(event.getPlayer());
        } else fPlayer.setBlock(event.getPlayer().getLocation().getBlock());

        if (!config.getBoolean("command.mark.enable")) return;
        if (!event.getPlayer().hasPermission("flectonechat.mark")) return;
        if (event.getItem() == null) return;

        if (event.getItem().getType().equals(Material.NETHER_STAR) && event.getItem().getItemMeta() != null) {
            String itemName = event.getItem().getItemMeta().getDisplayName();
            if (itemName.equalsIgnoreCase("flectone")) {
                Bukkit.dispatchCommand(event.getPlayer(), "mark " + CommandMark.chatColorValues[((int) (Math.random() * CommandMark.chatColorValues.length))]);
                return;
            }
        }

        Material markItem;

        try {
            markItem = Material.valueOf(config.getString("command.mark.item").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            Main.warning("Item for mark was not found");
            markItem = Material.WOODEN_SWORD;
        }

        if (!event.getItem().getType().equals(markItem) || event.getItem().getItemMeta() == null) return;

        String itemName = event.getItem().getItemMeta().getDisplayName().toUpperCase();

        String command = "mark";

        if (!itemName.isEmpty() && Arrays.asList(CommandMark.chatColorValues).contains(itemName.toUpperCase())) {
            command += " " + itemName;
        }

        Bukkit.dispatchCommand(event.getPlayer(), command);
    }
}
