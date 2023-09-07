package net.flectone.listeners;

import net.flectone.managers.FileManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

public class PlayerEditBookListener implements Listener {

    @EventHandler
    public void onPlayerEditBook(@NotNull PlayerEditBookEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (!FileManager.config.getBoolean("chat.book-formatting.enable")
                || !player.hasPermission("flectonechat.chat.book-formatting")) return;

        BookMeta bookMeta = event.getNewBookMeta();
        String command = "book";
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        for (int x = 1; x <= event.getNewBookMeta().getPages().size(); x++) {
            String string = bookMeta.getPage(x);

            if (string.isEmpty()) continue;

            bookMeta.setPage(x, ObjectUtil.buildFormattedMessage(player, command, string, itemInHand));
        }

        if (event.isSigning()) {
            bookMeta.setTitle(ObjectUtil.buildFormattedMessage(player, command, bookMeta.getTitle(), itemInHand));
        }

        event.setNewBookMeta(bookMeta);
    }
}
