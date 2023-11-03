package net.flectone.chat.module.playerMessage.anvil;

import net.flectone.chat.builder.MessageBuilder;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.commands.SpyListener;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class AnvilListener extends FListener {
    public AnvilListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void anvilEvent(@NotNull InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory)
                || event.getSlot() != 2
                || event.getCurrentItem() == null
                || event.getCurrentItem().getItemMeta() == null
                || !(event.getWhoClicked() instanceof Player player)) return;

        if (event.isCancelled()) return;
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

        ItemStack itemStack = event.getCurrentItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName = itemMeta.getDisplayName();
        if (displayName.isEmpty()) return;

        List<String> features = config.getVaultStringList(player, getModule() + ".features");
        MessageBuilder messageBuilder = new MessageBuilder(player, itemStack, displayName, features);
        displayName = messageBuilder.getMessage(String.valueOf(ChatColor.ITALIC));

        if (ChatColor.stripColor(displayName).isEmpty()) return;

        SpyListener.send(player, "anvil", displayName);

        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
    }
}
