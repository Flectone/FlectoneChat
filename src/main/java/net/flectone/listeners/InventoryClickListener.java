package net.flectone.listeners;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void inventoryClick(@NotNull InventoryClickEvent event) {
        Player whoClicked = (Player) event.getWhoClicked();
        FPlayer fPlayer = FPlayerManager.getPlayer(whoClicked);
        if (fPlayer == null) return;
        List<Inventory> inventoryList = fPlayer.getInventoryList();

        if (!inventoryList.contains(event.getInventory()) || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        switch (clickedItem.getType()) {
            case PLAYER_HEAD -> {

                String secondPlayerName = clickedItem.getItemMeta().getLocalizedName();
                FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(secondPlayerName);
                if (secondFPlayer == null) return;

                Bukkit.dispatchCommand(whoClicked, "ignore " + secondFPlayer.getRealName());

                event.getInventory().remove(event.getCurrentItem());

                whoClicked.closeInventory();

                for (int x = 0; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        fPlayer.setNumberLastInventory(x);
                        break;
                    }
                }

                Bukkit.dispatchCommand(whoClicked, "ignore-list");

            }
            case SPECTRAL_ARROW -> {
                whoClicked.closeInventory();

                for (int x = 0; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        whoClicked.openInventory(inventoryList.get(x + 1));
                        break;
                    }
                }
            }
            case ARROW -> {
                whoClicked.closeInventory();

                for (int x = 1; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        whoClicked.openInventory(inventoryList.get(x - 1));
                        break;
                    }
                }
            }
        }

    }
}
