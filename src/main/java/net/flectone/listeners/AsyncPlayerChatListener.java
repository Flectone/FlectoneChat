package net.flectone.listeners;

import net.flectone.custom.FCommands;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import net.flectone.Main;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AsyncPlayerChatListener implements Listener {

    private String noRecipientsMessage = "";

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Set<Player> recipients = new HashSet<>(event.getRecipients());
        String message = event.getMessage();
        String globalPrefix = Main.locale.getString("chat.global.prefix");

        recipients.removeIf(recipient -> FPlayerManager.getPlayer(recipient).isIgnored(player));

        String chatType = message.startsWith(globalPrefix) && !message.equals(globalPrefix) && Main.config.getBoolean("chat.global.enable") ? "global" : "local";

        if (chatType.equals("local")) {
            int localRange = Main.config.getInt("chat.local.range");

            recipients.removeIf(recipient -> (player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localRange));

            if(recipients.size() == 1 && Main.config.getBoolean("chat.local.no-recipients.enable")){
                noRecipientsMessage = Main.locale.getFormatString("chat.local.no-recipients", player);
            }

            if(Main.config.getBoolean("chat.local.admin-see.enable")){
                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(onlinePlayer -> onlinePlayer.hasPermission("flectonechat.local.admin_see") || onlinePlayer.isOp())
                        .forEach(recipients::add);
            }

            if(Main.config.getBoolean("chat.local.set-cancelled")) event.setCancelled(true);

        } else {
            if(Main.config.getBoolean("chat.global.prefix.cleared")) event.setMessage(message.replaceFirst(globalPrefix, ""));
            message = message.replaceFirst(globalPrefix + " ", "").replaceFirst(globalPrefix, "");
        }

        createMessage(recipients, player, message, chatType);
        event.getRecipients().clear();
    }


    public void createMessage(Set<Player> recipients, Player player, String message, String chatType){
        createMessage(recipients, player, message, chatType, null);
    }

    public void createMessage(Set<Player> recipients, Player player, String message, String chatType, ItemStack itemStack){

        FCommands fCommands = new FCommands(player, chatType + "chat", chatType + " chat", new String[]{});

        if(fCommands.isHaveCD()) return;

        if(fCommands.isMuted()) return;

        if(!noRecipientsMessage.isEmpty()) player.sendMessage(noRecipientsMessage);

        String configMessage = Main.locale.getString("chat." + chatType + ".message")
                .replace("<player>", FPlayerManager.getPlayer(player).getName());

        fCommands.sendGlobalMessage(recipients, configMessage, message, itemStack, true);

    }

    @EventHandler
    public void checkItemTooltipShortcut(InventoryClickEvent event) {
        if (event.getSlot() != 39 || !event.isShiftClick() || event.getCursor().getType().equals(Material.AIR)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        Set<Player> recipients = player.getWorld().getNearbyEntities(player.getLocation(), 100, 100, 100)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toSet());

        recipients.removeIf(recipient -> FPlayerManager.getPlayer(recipient).isIgnored(player));

        createMessage(recipients, player, "%item%", "local", event.getCursor());
    }
}
