package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.commands.CommandMark;
import net.flectone.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void playerItemClick(PlayerInteractEvent event){

        if(PlayerManager.getPlayer(event.getPlayer()).isAfk()){
            CommandAfk.setAfkFalse(event.getPlayer());
        } else PlayerManager.getPlayer(event.getPlayer()).setLastBlock(event.getPlayer().getLocation().getBlock());

        if(!Main.config.getBoolean("mark.enable")) return;
        if(!event.getPlayer().hasPermission("flectonechat.mark")) return;

        if(event.getItem() == null) return;

        if(event.getItem().getType().equals(Material.NETHER_STAR)){
            String itemName = event.getItem().getItemMeta().getDisplayName();
            if(!itemName.isEmpty() && itemName.toLowerCase().equals("flectone")){
                Bukkit.dispatchCommand(event.getPlayer(), "mark " + CommandMark.chatColorValues[((int) (Math.random()* CommandMark.chatColorValues.length))]);
                return;
            }
        }

        Material markItem;

        try {
            markItem = Material.valueOf(Main.config.getString("mark.item").toUpperCase());

        } catch (IllegalArgumentException | NullPointerException exception ){
            Main.getInstance().getLogger().warning("Item for mark was not found");
            markItem = Material.WOODEN_SWORD;
        }

        if(!event.getItem().getType().equals(markItem)) return;

        String itemName = event.getItem().getItemMeta().getDisplayName().toUpperCase();

        String command = "mark";

        if(!itemName.isEmpty() && Arrays.asList(CommandMark.chatColorValues).contains(itemName)){
            command += " " + itemName;
        }

        Bukkit.dispatchCommand(event.getPlayer(), command);

    }
}
