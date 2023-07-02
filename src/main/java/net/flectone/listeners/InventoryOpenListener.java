package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class InventoryOpenListener implements Listener {

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event){

        List<Inventory> inventoryList = PlayerManager.getPlayer(event.getPlayer()).getInventoryList();
        if(inventoryList == null
                || !inventoryList.contains(event.getInventory())) return;


        List<String> ignoreList = PlayerManager.getPlayer(event.getPlayer()).getIgnoreList();

        int indexItem = 0;
        int numberInventory = 0;
        int maxSlotsInventory = 17;
        for(int y = 0; y < ignoreList.size(); y++){

            if(y > maxSlotsInventory){
                maxSlotsInventory += 18;
                indexItem = 0;
                numberInventory++;
            }

            ItemStack blockForHead = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta skullMeta = (SkullMeta) blockForHead.getItemMeta();

            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(ignoreList.get(y)));

            skullMeta.setDisplayName("§e" + player.getName());
            skullMeta.setLocalizedName(player.getName());
            skullMeta.setOwner(player.getName());
            blockForHead.setItemMeta(skullMeta);
            inventoryList.get(numberInventory).setItem(indexItem++, blockForHead);

        }

        for(int x = 0; x < inventoryList.size(); x++){
            Inventory inventory = inventoryList.get(x);

            if(inventoryList.size() > x+1){
                inventory.setItem(26, createArrowItem(new ItemStack(Material.SPECTRAL_ARROW), "spectral_arrow", event.getPlayer()));
            }
            if(x != 0){
                inventory.setItem(18, createArrowItem(new ItemStack(Material.ARROW), "arrow", event.getPlayer()));
            }
        }
    }

    private ItemStack createArrowItem(ItemStack arrow, String arrowName, HumanEntity player){
        ItemMeta itemMeta = arrow.getItemMeta();
        itemMeta.setDisplayName(Main.locale.getFormatString("ignore-list." + arrowName, (Player) player));
        arrow.setItemMeta(itemMeta);
        return arrow;
    }
}
