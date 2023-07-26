package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilClickListener implements Listener {

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory)
                || event.getSlot() != 2
                || event.getCurrentItem() == null
                || event.getCurrentItem().getItemMeta() == null
                || !(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String command = "anvil";

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName = itemMeta.getDisplayName();

        itemMeta.setDisplayName(ObjectUtil.buildFormattedMessage(player, command, displayName, itemStack));
        itemStack.setItemMeta(itemMeta);
    }
}
