package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.commands.CommandMark;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerInteractListener implements Listener {

    private static final Map<Material, String> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(Material.WHITE_DYE, "#FFFFFF");
        COLOR_MAP.put(Material.GRAY_DYE, "#999999");
        COLOR_MAP.put(Material.LIGHT_GRAY_DYE, "#CCCCCC");
        COLOR_MAP.put(Material.BLACK_DYE, "#333333");
        COLOR_MAP.put(Material.RED_DYE, "#FF3333");
        COLOR_MAP.put(Material.ORANGE_DYE, "#FF9900");
        COLOR_MAP.put(Material.YELLOW_DYE, "#FFFF00");
        COLOR_MAP.put(Material.LIME_DYE, "#33FF33");
        COLOR_MAP.put(Material.GREEN_DYE, "#009900");
        COLOR_MAP.put(Material.LIGHT_BLUE_DYE, "#99CCFF");
        COLOR_MAP.put(Material.CYAN_DYE, "#33CCCC");
        COLOR_MAP.put(Material.BLUE_DYE, "#3366FF");
        COLOR_MAP.put(Material.PURPLE_DYE, "#9900CC");
        COLOR_MAP.put(Material.MAGENTA_DYE, "#FF66FF");
        COLOR_MAP.put(Material.PINK_DYE, "#FF99CC");
        COLOR_MAP.put(Material.BROWN_DYE, "#CC6600");
    }

    @EventHandler
    public void playerItemClick(PlayerInteractEvent event) {

        if (FPlayerManager.getPlayer(event.getPlayer()).isAfk()) {
            CommandAfk.setAfkFalse(event.getPlayer());
        } else FPlayerManager.getPlayer(event.getPlayer()).setBlock(event.getPlayer().getLocation().getBlock());

        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && Main.config.getBoolean("player.item.sign.enable")
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType().equals(Material.ANVIL)) {

            PlayerInventory playerInventory = event.getPlayer().getInventory();
            ItemStack offHandItem = playerInventory.getItemInOffHand();
            ItemStack mainHandItem = playerInventory.getItemInMainHand();

            String color = COLOR_MAP.get(offHandItem.getType());

            if (color != null && mainHandItem.getType() != Material.AIR) {
                ItemStack finalItem = mainHandItem.clone();
                finalItem.setAmount(1);

                paintItem(finalItem, event.getPlayer().getName(), color);

                decreaseItemAmount(mainHandItem, () -> playerInventory.setItemInMainHand(null));
                decreaseItemAmount(offHandItem, () -> playerInventory.setItemInOffHand(null));

                Location location = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
                event.getClickedBlock().getWorld().dropItem(location, finalItem);
            }
        }

        if (!Main.config.getBoolean("command.mark.enable")) return;
        if (!event.getPlayer().hasPermission("flectonechat.mark")) return;

        if (event.getItem() == null) return;

        if (event.getItem().getType().equals(Material.NETHER_STAR)) {
            String itemName = event.getItem().getItemMeta().getDisplayName();
            if (itemName.equalsIgnoreCase("flectone")) {
                Bukkit.dispatchCommand(event.getPlayer(), "mark " + CommandMark.chatColorValues[((int) (Math.random() * CommandMark.chatColorValues.length))]);
                return;
            }
        }

        Material markItem;

        try {
            markItem = Material.valueOf(Main.config.getString("command.mark.item").toUpperCase());

        } catch (IllegalArgumentException | NullPointerException exception) {
            Main.getInstance().getLogger().warning("Item for mark was not found");
            markItem = Material.WOODEN_SWORD;
        }

        if (!event.getItem().getType().equals(markItem)) return;

        String itemName = event.getItem().getItemMeta().getDisplayName().toUpperCase();

        String command = "mark";

        if (!itemName.isEmpty() && containsColor(itemName)) {
            command += " " + itemName;
        }

        Bukkit.dispatchCommand(event.getPlayer(), command);

    }

    private boolean containsColor(String color) {
        return Arrays.asList(CommandMark.chatColorValues).contains(color.toUpperCase());
    }

    private void paintItem(ItemStack itemStack, String playerName, String color) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> stringList = itemMeta.getLore();

        if (stringList == null) {
            stringList = new ArrayList<>();
        }

        String formatString = Main.locale.getString("player.item.sign")
                .replace("<player>", playerName);

        formatString = ObjectUtil.translateHexToColor(formatString);

        int numberPaint = -1;
        for (int x = 0; x < stringList.size(); x++) {
            if (stringList.get(x).contains(formatString)) {
                numberPaint = x;
                break;
            }
        }

        formatString = ObjectUtil.translateHexToColor(color + formatString);

        if (numberPaint != -1) {
            stringList.set(numberPaint, formatString);
        } else {
            stringList.add(formatString);
        }

        itemMeta.setLore(stringList);
        itemStack.setItemMeta(itemMeta);
    }

    private void decreaseItemAmount(ItemStack itemStack, ReplaceItem replaceItem) {
        if (itemStack.getAmount() == 1) {
            replaceItem.setItemNull();
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    private interface ReplaceItem {
        void setItemNull();
    }
}
