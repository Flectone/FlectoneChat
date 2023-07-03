package net.flectone.listeners;

import net.flectone.custom.FCommands;
import net.flectone.managers.FileManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import net.flectone.Main;
import net.flectone.managers.PlayerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AsyncPlayerChatListener implements Listener {

    private FileManager locale = Main.locale;

    private FileManager config = Main.config;

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Set<Player> recipients = new HashSet<>(event.getRecipients());
        String message = event.getMessage();
        String globalPrefix = config.getString("chat.global.prefix");

        recipients.removeIf(recipient -> PlayerManager.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString()));

        String chatType = message.startsWith(globalPrefix) && !message.equals(globalPrefix) && config.getBoolean("chat.global.enable") ? "global" : "locale";

        if (chatType.equals("locale")) {
            int localeRange = config.getInt("chat.locale.range");
            recipients.removeIf(recipient -> player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localeRange);

            if(Main.config.getBoolean("chat.locale.set_cancelled")) event.setCancelled(true);

        } else {
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

        if (chatType.equals("locale") && recipients.size() == 1 && config.getBoolean("chat.no_recipients.enable")) {
            player.sendMessage(locale.getFormatString("chat.no_recipients", player));
        }

        String configMessage = config.getString("chat." + chatType + ".message")
                .replace("<player>", PlayerManager.getPlayer(player).getName());

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

        recipients.removeIf(recipient -> PlayerManager.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString()));

        createMessage(recipients, player, "%item%", "locale", event.getCursor());
    }
}
