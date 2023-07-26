package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AsyncPlayerChatListener implements Listener {

    private String noRecipientsMessage = "";

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        String fPlayerChat = FPlayerManager.getPlayer(player).getChat();
        String globalPrefix = Main.locale.getString("chat.global.prefix");
        String message = event.getMessage();

        String chatType = Main.config.getBoolean("chat.global.enable")
                && (fPlayerChat.contains("global")
                || message.startsWith(globalPrefix) && !message.equals(globalPrefix) && !fPlayerChat.equals("onlylocal"))
                ? "global" : "local";

        String reversedChatType = chatType.equals("global") ? "local" : "global";

        Set<Player> recipients = new HashSet<>(event.getRecipients());
        removeRecipients(recipients, player, reversedChatType);

        if (chatType.equals("local")) {
            int localRange = Main.config.getInt("chat.local.range");

            recipients.removeIf(recipient -> (player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localRange));

            if (recipients.stream().filter(recipient -> !recipient.getGameMode().equals(GameMode.SPECTATOR)).count() == 1
                    && Main.config.getBoolean("chat.local.no-recipients.enable")) {
                noRecipientsMessage = Main.locale.getFormatString("chat.local.no-recipients", player);
            }

            if (Main.config.getBoolean("chat.local.set-cancelled")) event.setCancelled(true);

        } else {
            if (Main.config.getBoolean("chat.global.prefix.cleared"))
                event.setMessage(message.replaceFirst(globalPrefix, ""));
            if (Main.config.getBoolean("chat.global.set-cancelled")) event.setCancelled(true);
            message = message.replaceFirst(globalPrefix + " ", "").replaceFirst(globalPrefix, "");
        }

        createMessage(recipients, player, message, chatType, null);
        event.getRecipients().clear();
    }

    public void createMessage(Set<Player> recipients, Player player, String message, String chatType, ItemStack itemStack) {

        if (chatType.equals("local") && Main.config.getBoolean("chat.local.admin-see.enable")) {
            Bukkit.getOnlinePlayers().parallelStream()
                    .filter(onlinePlayer -> onlinePlayer.hasPermission("flectonechat.local.admin_see"))
                    .forEach(recipients::add);
        }

        FCommands fCommands = new FCommands(player, chatType + "chat", chatType + " chat", new String[]{});

        if (fCommands.isHaveCD()) return;

        if (fCommands.isMuted()) return;

        if (!noRecipientsMessage.isEmpty()) {
            player.sendMessage(noRecipientsMessage);
            noRecipientsMessage = "";
        }

        String configMessage = Main.locale.getString("chat." + chatType + ".message")
                .replace("<player>", FPlayerManager.getPlayer(player).getDisplayName());

        fCommands.sendGlobalMessage(recipients, configMessage, message, itemStack, true);
    }

    @EventHandler
    public void checkItemTooltipShortcut(InventoryClickEvent event) {
        if (event.getSlot() != 39 || !event.isShiftClick() || event.getCursor().getType().equals(Material.AIR)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        String chatType = Main.config.getBoolean("chat.global.enable")
                && FPlayerManager.getPlayer(player).getChat().contains("global")
                ? "global" : "local";
        String reversedChatType = chatType.equals("global") ? "local" : "global";

        int localRange = Main.config.getInt("chat.local.range");
        Set<Player> recipients = player.getWorld().getNearbyEntities(player.getLocation(), localRange, localRange, localRange)
                .parallelStream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toSet());

        removeRecipients(recipients, player, reversedChatType);

        createMessage(recipients, player, "%item%", chatType, event.getCursor());
    }

    private void removeRecipients(Set<Player> recipients, Player player, String reversedChatType) {
        recipients.removeIf(recipient -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(recipient);

            return fPlayer.isIgnored(player) || fPlayer.getChat().equals("only" + reversedChatType);
        });
    }
}
