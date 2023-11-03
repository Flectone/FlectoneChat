package net.flectone.chat.module.playerMessage.book;


import net.flectone.chat.builder.MessageBuilder;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.commands.SpyListener;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class BookListener extends FListener {
    public BookListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void bookEvent(@NotNull PlayerEditBookEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;

        FPlayer fPlayer = FPlayerManager.get(player);

        if (fPlayer != null && fPlayer.isMuted()) {
            String message = locale.getVaultString(fPlayer.getPlayer(), "commands.muted");

            Moderation mute = fPlayer.getMute();
            message = message
                    .replace("<time>", TimeUtil.convertTime(fPlayer.getPlayer(), mute.getTime() - TimeUtil.getCurrentTime()))
                    .replace("<reason>", mute.getReason())
                    .replace("<moderator>", mute.getModeratorName());

            message = MessageUtil.formatAll(fPlayer.getPlayer(), message);

            player.sendMessage(message);

            event.setCancelled(true);
        }

        BookMeta bookMeta = event.getNewBookMeta();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        List<String> features = config.getVaultStringList(player, getModule() + ".features");

        for (int x = 1; x <= event.getNewBookMeta().getPages().size(); x++) {
            String string = bookMeta.getPage(x);

            if (string.isEmpty()) continue;

            MessageBuilder messageBuilder = new MessageBuilder(player, itemInHand, string, features);
            bookMeta.setPage(x, messageBuilder.getMessage(""));
            SpyListener.send(player, "book", messageBuilder.getMessage(""));
        }

        if (event.isSigning() && bookMeta.getTitle() != null) {
            MessageBuilder messageBuilder = new MessageBuilder(player, itemInHand, bookMeta.getTitle(), features);
            bookMeta.setTitle(messageBuilder.getMessage(""));
            SpyListener.send(player, "book", messageBuilder.getMessage(""));
        }

        event.setNewBookMeta(bookMeta);
    }
}
