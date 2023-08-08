package net.flectone.listeners;

import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SignChangeListener implements Listener {

    @EventHandler
    public void onSignChange(@NotNull SignChangeEvent event) {
        Player player = event.getPlayer();
        String command = "sign";
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        for (int x = 0; x < event.getLines().length; x++) {
            String string = event.getLine(x);

            if (string == null || string.isEmpty()) continue;

            event.setLine(x, ObjectUtil.buildFormattedMessage(player, command, string, itemInHand));
        }
    }
}
