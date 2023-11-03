package net.flectone.chat.module.extra.mark;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.model.mark.Mark.COLOR_VALUES;

public class MarkListener extends FListener {


    public MarkListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void playerJoinEvent(@NotNull PlayerJoinEvent event) {
        ((MarkModule) getModule()).removeBugEntities(event.getPlayer());
    }

    @EventHandler
    public void markEvent(@NotNull PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getItemMeta() == null) return;

        Player player = event.getPlayer();
        if (!config.getVaultBoolean(player, getModule() + ".enable")) return;
        if (hasNoPermission(player)) return;

        Material triggerMaterial;

        try {
            triggerMaterial = Material.valueOf(config.getVaultString(player, getModule() + ".item").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            FlectoneChat.warning("Item for mark was not found");
            triggerMaterial = Material.WOODEN_SWORD;
        }

        if (!itemStack.getType().equals(triggerMaterial)) return;

        String itemName = itemStack.getItemMeta().getDisplayName().toUpperCase();
        String color = COLOR_VALUES.contains(itemName)
                ? itemName
                : "WHITE";

        int range = config.getVaultInt(player,getModule() + ".range");

        ((MarkModule) getModule()).mark(player, range, color);
    }
}
