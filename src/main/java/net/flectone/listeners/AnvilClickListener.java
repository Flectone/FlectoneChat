package net.flectone.listeners;

import net.flectone.messages.MessageBuilder;
import net.flectone.utils.ObjectUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilClickListener implements Listener {

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event){
        if(!(event.getClickedInventory() instanceof AnvilInventory)
                || event.getSlot() != 2
                || event.getCurrentItem() == null
                || event.getCurrentItem().getItemMeta() == null) return;

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName = itemMeta.getDisplayName();

        MessageBuilder messageBuilder = new MessageBuilder("anvilitem", displayName, itemStack, false);
        displayName = messageBuilder.getMessage();

        if(event.getWhoClicked().isOp() || event.getWhoClicked().hasPermission("flectonechat.formatting")){
            displayName = ObjectUtil.formatString(displayName, event.getWhoClicked());
        }

        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
    }
}
