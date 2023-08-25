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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

    @EventHandler
    public void chat(@NotNull AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer == null) return;

        String fPlayerChat = fPlayer.getChatInfo().getChatType();
        String globalPrefix = locale.getString("chat.global.prefix");
        String message = event.getMessage();

        String chatType = config.getBoolean("chat.global.enable")
                && (fPlayerChat.contains("global")
                || message.startsWith(globalPrefix) && !message.equals(globalPrefix) && !fPlayerChat.equals("onlylocal"))
                ? "global" : "local";

        String reversedChatType = chatType.equals("global") ? "local" : "global";

        Set<Player> recipients = new HashSet<>(event.getRecipients());
        removeRecipients(recipients, player, reversedChatType);

        if (chatType.equals("local")) {
            if(config.getBoolean("chat.global.enable")) {
                int localRange = config.getInt("chat.local.range");
                recipients.removeIf(recipient -> (player.getWorld() != recipient.getWorld()
                        || player.getLocation().distance(recipient.getLocation()) > localRange));

                if (config.getBoolean("chat.local.no-recipients.enable") &&
                        recipients.stream().filter(recipient -> !recipient.getGameMode().equals(GameMode.SPECTATOR)).count() == 1) {
                    noRecipientsMessage = locale.getFormatString("chat.local.no-recipients", player);
                }

            } else if(HookManager.enabledInteractiveChat) message = FInteractiveChat.checkMention(event);

            if (config.getBoolean("chat.local.set-cancelled")) event.setCancelled(true);

        } else {
            if(HookManager.enabledInteractiveChat) message = FInteractiveChat.checkMention(event);

            if (config.getBoolean("chat.global.prefix.cleared")) event.setMessage(message.replaceFirst(globalPrefix, ""));
            if (config.getBoolean("chat.global.set-cancelled")) event.setCancelled(true);
            message = message.replaceFirst(globalPrefix, "").trim();
        }

        createMessage(recipients, player, message, chatType, null);
        event.getRecipients().clear();
    }

    public void createMessage(@NotNull Set<Player> recipients, @NotNull Player player, @NotNull String message, @NotNull String chatType, @Nullable ItemStack itemStack) {

        if (chatType.equals("local") && config.getBoolean("chat.local.admin-see.enable")) {
            Bukkit.getOnlinePlayers().parallelStream()
                    .filter(onlinePlayer -> onlinePlayer.hasPermission("flectonechat.local.admin_see"))
                    .forEach(recipients::add);
        }

        FCommand fCommand = new FCommand(player, chatType + "chat", chatType + " chat", new String[]{});

        if (fCommand.isHaveCD() || fCommand.isMuted()) return;

        if (!noRecipientsMessage.isEmpty()) {
            player.sendMessage(noRecipientsMessage);
            noRecipientsMessage = "";
        }

        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer == null) return;

        String configMessage = locale.getString("chat." + chatType + ".message")
                .replace("<player>", fPlayer.getDisplayName());

        fCommand.sendGlobalMessage(recipients, configMessage, message, itemStack, true);
    }

    @EventHandler
    public void checkItemTooltipShortcut(@NotNull InventoryClickEvent event) {
        if (event.getSlot() != 39
                || !event.isShiftClick()
                || event.getCursor() == null
                || event.getCursor().getType().equals(Material.AIR)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if(fPlayer == null) return;

        String chatType = config.getBoolean("chat.global.enable")
                && fPlayer.getChatInfo().getChatType().contains("global")
                ? "global" : "local";
        String reversedChatType = chatType.equals("global") ? "local" : "global";

        int localRange = config.getInt("chat.local.range");
        Set<Player> recipients = player.getWorld().getNearbyEntities(player.getLocation(), localRange, localRange, localRange)
                .parallelStream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toSet());

        removeRecipients(recipients, player, reversedChatType);

        createMessage(recipients, player, "%item%", chatType, event.getCursor());
    }

    private void removeRecipients(@NotNull Set<Player> recipients, @NotNull Player player, @NotNull String reversedChatType) {
        recipients.removeIf(recipient -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(recipient);
            if(fPlayer == null) return false;

            return fPlayer.isIgnored(player) || fPlayer.getChatInfo().getChatType().equals("only" + reversedChatType);
        });
    }
}
