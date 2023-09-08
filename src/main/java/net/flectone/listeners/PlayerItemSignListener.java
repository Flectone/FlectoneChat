package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class PlayerItemSignListener implements Listener {

    private static final Map<Material, String> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(Material.WHITE_DYE, "#ffffff");
        COLOR_MAP.put(Material.GRAY_DYE, "#999999");
        COLOR_MAP.put(Material.LIGHT_GRAY_DYE, "#cccccc");
        COLOR_MAP.put(Material.BLACK_DYE, "#333333");
        COLOR_MAP.put(Material.RED_DYE, "#ff3333");
        COLOR_MAP.put(Material.ORANGE_DYE, "#ff9900");
        COLOR_MAP.put(Material.YELLOW_DYE, "#ffff00");
        COLOR_MAP.put(Material.LIME_DYE, "#33ff33");
        COLOR_MAP.put(Material.GREEN_DYE, "#009900");
        COLOR_MAP.put(Material.LIGHT_BLUE_DYE, "#99ccff");
        COLOR_MAP.put(Material.CYAN_DYE, "#33cccc");
        COLOR_MAP.put(Material.BLUE_DYE, "#3366ff");
        COLOR_MAP.put(Material.PURPLE_DYE, "#9900cc");
        COLOR_MAP.put(Material.MAGENTA_DYE, "#ff66ff");
        COLOR_MAP.put(Material.PINK_DYE, "#ff99cc");
        COLOR_MAP.put(Material.BROWN_DYE, "#cc6600");
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (!config.getBoolean("player.item.sign.enable")) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        PlayerInventory playerInventory = event.getPlayer().getInventory();
        ItemStack offHandItem = playerInventory.getItemInOffHand();
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        if (mainHandItem.getType() == Material.AIR) return;

        String signTemplate = locale.getFormatString("player.item.sign", event.getPlayer())
                .replace("<player>", event.getPlayer().getName());
        String updatedSignText = signTemplate;

        String colorHex = COLOR_MAP.get(offHandItem.getType());
        boolean shouldRemoveSign = false;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && isAnvil(clickedBlock.getType()) && colorHex != null) {
            updatedSignText = ObjectUtil.translateHexToColor(colorHex + signTemplate);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock.getBlockData() instanceof Levelled) {
            shouldRemoveSign = true;
        } else {
            return;
        }

        ItemStack itemToDrop = mainHandItem.clone();
        itemToDrop.setAmount(1);

        int signIndex = addOrRemoveSignature(itemToDrop, updatedSignText, shouldRemoveSign);
        if (signIndex == -1) return;

        Location dropLocation = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

        if (signIndex != -2 && mainHandItem.getItemMeta() != null && mainHandItem.getItemMeta().getLore() != null) {
            String signText = mainHandItem.getItemMeta().getLore().get(signIndex);

            COLOR_MAP.entrySet().stream()
                    .filter(entry -> ObjectUtil.translateHexToColor(entry.getValue() + signTemplate).equalsIgnoreCase(signText))
                    .findFirst()
                    .ifPresent(colorEntry -> event.getClickedBlock().getWorld().dropItem(dropLocation, new ItemStack(colorEntry.getKey())));
        }

        if (!shouldRemoveSign) decreaseItemAmount(offHandItem, () -> playerInventory.setItemInOffHand(null));
        decreaseItemAmount(mainHandItem, () -> playerInventory.setItemInMainHand(null));

        event.getClickedBlock().getWorld().dropItem(dropLocation, itemToDrop);

        event.setCancelled(true);
    }

    private boolean isAnvil(@NotNull Material material) {
        return material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL;
    }

    /**
     * Adds or removes a signature string to/from the item's metadata.
     *
     * @param itemStack The item to which the signature string will be added or from which it will be removed.
     * @param signature The signature string to add or remove.
     * @param removeFromLore True if the signature should be removed from the lore, false if it should be added.
     * @return The index of the signature string in the lore, <br> or -1 if not found, <br> or -2 if added.
     */
    private int addOrRemoveSignature(@NotNull ItemStack itemStack, @NotNull String signature, boolean removeFromLore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return -1;

        List<String> loreList = itemMeta.getLore();
        if (loreList == null) {
            loreList = new ArrayList<>();
        }

        String signatureWithoutColor = ChatColor.stripColor(signature);
        int signatureIndex = -1;
        for (int index = 0; index < loreList.size(); index++) {
            if (loreList.get(index).endsWith(signatureWithoutColor)) {
                signatureIndex = index;
                break;
            }
        }

        if (signatureIndex != -1) {
            if (removeFromLore) {
                loreList.remove(signatureIndex);
            } else {
                loreList.set(signatureIndex, signature);
            }
        } else {
            if (removeFromLore) return -1;
            loreList.add(signature);
            signatureIndex = -2;
        }

        itemMeta.setLore(loreList);
        itemStack.setItemMeta(itemMeta);

        return signatureIndex;
    }

    /**
     * Decreases the item's amount and handles replacement when the amount reaches 1.
     *
     * @param itemStack The item whose amount needs to be decreased.
     * @param itemReplacementHandler The handler to set the item to null when the amount reaches 1.
     */
    private void decreaseItemAmount(@NotNull ItemStack itemStack, @NotNull ItemReplacementHandler itemReplacementHandler) {
        if (itemStack.getAmount() == 1) {
            itemReplacementHandler.setItemToNull();
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    @FunctionalInterface
    private interface ItemReplacementHandler {
        /**
         * Sets the item to null.
         */
        void setItemToNull();
    }
}
