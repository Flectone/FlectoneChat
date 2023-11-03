package net.flectone.chat.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemUtil {

    /**
     * Decreases the item's amount and handles replacement when the amount reaches 1.
     *
     * @param itemStack The item whose amount needs to be decreased.
     * @param itemReplacementHandler The handler to set the item to null when the amount reaches 1.
     */
    public static void decreaseItemAmount(@NotNull ItemStack itemStack, @NotNull ItemReplacementHandler itemReplacementHandler) {
        if (itemStack.getAmount() == 1) {
            itemReplacementHandler.setItemToNull();
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    @FunctionalInterface
    public interface ItemReplacementHandler {
        /**
         * Sets the item to null.
         */
        void setItemToNull();
    }

}
