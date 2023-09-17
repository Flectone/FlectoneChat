package net.flectone.listeners;

import net.flectone.integrations.interactivechat.FInteractiveChat;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class AsyncPlayerChatListener implements Listener {

    private String noRecipientsMessage = "";

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(@NotNull AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer == null) return;

        String fPlayerChat = fPlayer.getChatInfo().getChatType();
        String globalPrefix = locale.getString("chat.global.prefix");
        String message = event.getMessage();

        String chatType = fPlayerChat.contains("global")
                || message.startsWith(globalPrefix)
                            && !message.equals(globalPrefix)
                            && !fPlayerChat.equals("onlylocal")
                ? "global"
                : "local";

        String reversedChatType = chatType.equals("global") ? "local" : "global";

        if (!config.getBoolean("chat." + chatType + ".enable")) {
            if (!config.getBoolean("chat." + reversedChatType + ".enable")) {
                player.sendMessage(locale.getFormatString("chat.all-disabled", player));
                event.setCancelled(true);
                return;
            }

            String tempString = chatType;
            chatType = reversedChatType;
            reversedChatType = tempString;
        }

        Set<Player> recipients = new HashSet<>(event.getRecipients());
        removeRecipients(recipients, player, reversedChatType);

        if (chatType.equals("local")) {

            int localRange = config.getInt("chat.local.range");
            recipients.removeIf(recipient -> (player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localRange));

            if (config.getBoolean("chat.local.no-recipients.enable") &&
                    recipients.stream().filter(recipient -> !recipient.getGameMode().equals(GameMode.SPECTATOR)).count() == 1) {
                noRecipientsMessage = locale.getFormatString("chat.local.no-recipients", player);
            }

        } else {
            if(HookManager.enabledInteractiveChat) message = FInteractiveChat.checkMention(event);

            if (config.getBoolean("chat.global.prefix.cleared")) event.setMessage(message.replaceFirst(globalPrefix, ""));

            message = message.replaceFirst(globalPrefix, "").trim();
        }

        if (config.getBoolean("chat." + chatType + ".set-cancelled")) event.setCancelled(true);

        createMessage(recipients, fPlayer, message, chatType, null);
        event.getRecipients().clear();
    }

    public void createMessage(@NotNull Set<Player> recipients, @NotNull FPlayer fPlayer, @NotNull String message, @NotNull String chatType, @Nullable ItemStack itemStack) {

        Player player = fPlayer.getPlayer();
        assert player != null;

        FCommand fCommand = new FCommand(player, chatType + "chat", chatType + " chat", message.split(" "));

        if (fCommand.isHaveCD() || fCommand.isMuted()) return;

        String configMessage = FPlayer.getVaultLocaleString(player, "chat." + chatType + ".<group>.message")
                .replace("<player>", fPlayer.getDisplayName());

        fCommand.sendFilterGlobalMessage(recipients, configMessage, message, itemStack, true);

        if (!noRecipientsMessage.isEmpty()) {
            player.sendMessage(noRecipientsMessage);
            noRecipientsMessage = "";
        }
    }

    @EventHandler
    public void checkItemTooltipShortcut(@NotNull InventoryClickEvent event) {
        if (event.isCancelled()
                || !(event.getInventory() instanceof CraftingInventory)
                || event.getSlot() != 39
                || !event.isShiftClick()
                || event.getCursor() == null
                || event.getCursor().getType().equals(Material.AIR)
                || !config.getBoolean("chat.tooltip.enable")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer == null) return;

        String chatType = config.getBoolean("chat.global.enable")
                && fPlayer.getChatInfo().getChatType().contains("global")
                ? "global" : "local";
        String reversedChatType = chatType.equals("global") ? "local" : "global";

        Set<Player> recipients;

        if (chatType.equals("local")) {
            int localRange = config.getInt("chat.local.range");
            recipients = player.getWorld().getNearbyEntities(player.getLocation(), localRange, localRange, localRange)
                    .parallelStream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .collect(Collectors.toSet());
        } else {
            recipients = new HashSet<>(Bukkit.getOnlinePlayers());
        }

        removeRecipients(recipients, player, reversedChatType);

        createMessage(recipients, fPlayer, "%item%", chatType, event.getCursor());
    }

    private void removeRecipients(@NotNull Set<Player> recipients, @NotNull Player player, @NotNull String reversedChatType) {
        recipients.removeIf(recipient -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(recipient);
            if(fPlayer == null) return false;

            return fPlayer.isIgnored(player) || fPlayer.getChatInfo().getChatType().equals("only" + reversedChatType);
        });
    }
}
