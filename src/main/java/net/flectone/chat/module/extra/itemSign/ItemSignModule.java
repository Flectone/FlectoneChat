package net.flectone.chat.module.extra.itemSign;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.ColorUtil;
import net.flectone.chat.util.ItemUtil;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemSignModule extends FModule {

    public final static NamespacedKey signKey = new NamespacedKey(FlectoneChat.getPlugin(), "flectonechat.sign");

    public ItemSignModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new ItemSignListener(this));

        if (!modules.getBoolean(this + ".unsign.enable")) return;
        actionManager.add(new ItemUnsignListener(this));
    }

    public boolean unsign(@NotNull Player player, @NotNull Location location, @NotNull PlayerInventory playerInventory,
                          boolean dropDyeEnabled) {
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        if (mainHandItem.getType() == Material.AIR) return false;

        ItemStack itemToDrop = mainHandItem.clone();
        itemToDrop.setAmount(1);

        ItemStack dye = removeSign(player, itemToDrop);
        if (dye == null) return false;

        ItemUtil.decreaseItemAmount(mainHandItem, () -> playerInventory.setItemInMainHand(null));

        Location dropLocation = location.add(0.5, 1, 0.5);
        dropLocation.getWorld().dropItem(dropLocation, itemToDrop);

        if (dropDyeEnabled) dropLocation.getWorld().dropItem(dropLocation, dye);
        return true;
    }

    @Nullable
    public ItemStack removeSign(@NotNull Player player, @NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) return null;

        Pair<Integer, int[]> indexAndArray = findSignIndexAndGetAll(itemMeta, itemLore, player);

        int index = indexAndArray.getKey();
        int[] signIndexes = indexAndArray.getValue();

        if (index == -1) return null;

        int[] newSignIndexes = new int[signIndexes.length - 1];

        int k = 0;
        for (int signIndex : signIndexes) {
            if (signIndex == index) continue;
            newSignIndexes[k++] = signIndex;
        }

        String sign = itemLore.get(index);
        itemLore.remove(index);
        itemMeta.setLore(itemLore);

        if (newSignIndexes.length == 0) {
            itemMeta.getPersistentDataContainer().remove(signKey);
        } else {
            itemMeta.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER_ARRAY, newSignIndexes);
        }

        itemStack.setItemMeta(itemMeta);

        return ColorUtil.hexToDye(sign);
    }

    public boolean sign(@NotNull Player player, @NotNull Location location, @NotNull PlayerInventory playerInventory,
                        @NotNull String signFormat, boolean dropDyeEnabled) {
        ItemStack offHandItem = playerInventory.getItemInOffHand();
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        if (mainHandItem.getType() == Material.AIR || offHandItem.getType() == Material.AIR) return false;

        String colorHex = ColorUtil.dyeToHex(offHandItem);
        if (colorHex == null) return false;

        signFormat = signFormat.replace("<dye>", colorHex);
        signFormat = MessageUtil.formatAll(player, signFormat);

        ItemStack itemToDrop = mainHandItem.clone();
        itemToDrop.setAmount(1);

        Integer signIndex = addSign(player, itemToDrop, signFormat);
        if (signIndex == null) return false;

        Location dropLocation = location.add(0.5, 1, 0.5);
        if (signIndex != -1 && dropDyeEnabled) {
            ItemStack dye = ColorUtil.hexToDye(signFormat);
            if (dye != null) {
                dropLocation.getWorld().dropItem(dropLocation, dye);
            }
        }

        ItemUtil.decreaseItemAmount(mainHandItem, () -> playerInventory.setItemInMainHand(null));
        ItemUtil.decreaseItemAmount(offHandItem, () -> playerInventory.setItemInOffHand(null));

        dropLocation.getWorld().dropItem(dropLocation, itemToDrop);
        return true;
    }

    public Integer addSign(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull String signFormat) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) itemLore = new ArrayList<>();

        Pair<Integer, int[]> indexAndArray = findSignIndexAndGetAll(itemMeta, itemLore, player);

        int index = indexAndArray.getKey();
        int[] signIndexes = indexAndArray.getValue();

        if (index == -1) {
            signIndexes = Arrays.copyOf(signIndexes, signIndexes.length + 1);
            signIndexes[signIndexes.length - 1] = itemLore.size();
            itemLore.add(signFormat);
        } else {
            itemLore.set(index, signFormat);
        }

        itemMeta.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER_ARRAY, signIndexes);

        itemMeta.setLore(itemLore);
        itemStack.setItemMeta(itemMeta);

        return index;
    }

    private Pair<Integer, int[]> findSignIndexAndGetAll(@NotNull ItemMeta itemMeta, @NotNull List<String> itemLore,
                                                        @NotNull Player player) {
        int index = -1;
        int[] signIndexes = itemMeta.getPersistentDataContainer().get(signKey, PersistentDataType.INTEGER_ARRAY);
        if (signIndexes != null) {
            for (int signIndex : signIndexes) {
                String loreString = ChatColor.stripColor(itemLore.get(signIndex));
                if (loreString.contains(player.getName())) {
                    index = signIndex;
                    break;
                }
            }
        } else signIndexes = new int[]{};

        return new Pair<>(index, signIndexes);
    }
}
