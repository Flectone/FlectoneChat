package net.flectone.listeners;

import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {

        if (FPlayerManager.getPlayer((OfflinePlayer) event.getWhoClicked()).getInventoryList() == null
                || !FPlayerManager.getPlayer((OfflinePlayer) event.getWhoClicked()).getInventoryList().contains(event.getInventory()))
            return;


        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;


        Player eventPlayer = (Player) event.getWhoClicked();
        FPlayer eventFPlayer = FPlayerManager.getPlayer(eventPlayer);

        ItemStack clickedItem = event.getCurrentItem();

        switch (clickedItem.getType()) {
            case PLAYER_HEAD: {

                String secondPlayerName = clickedItem.getItemMeta().getLocalizedName();
                FPlayer secondFPlayer = FPlayerManager.getPlayerFromName(secondPlayerName);

                Bukkit.dispatchCommand(eventPlayer, "ignore " + secondFPlayer.getRealName());

                event.getInventory().remove(event.getCurrentItem());

                eventPlayer.closeInventory();

                List<Inventory> inventoryList = eventFPlayer.getInventoryList();
                for (int x = 0; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        eventFPlayer.setNumberLastInventory(x);
                        break;
                    }
                }

                Bukkit.dispatchCommand(eventPlayer, "ignore-list");

                break;
            }

            case SPECTRAL_ARROW: {
                eventPlayer.closeInventory();

                List<Inventory> inventoryList = eventFPlayer.getInventoryList();

                for (int x = 0; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        eventPlayer.openInventory(inventoryList.get(x + 1));
                        break;
                    }
                }
                break;
            }

            case ARROW: {
                eventPlayer.closeInventory();

                List<Inventory> inventoryList = eventFPlayer.getInventoryList();

                for (int x = 1; x < inventoryList.size(); x++) {
                    if (inventoryList.get(x) == event.getClickedInventory()) {
                        eventPlayer.openInventory(inventoryList.get(x - 1));
                        break;
                    }
                }
                break;
            }
        }

    }
}
