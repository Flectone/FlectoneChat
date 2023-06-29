package ru.flectone.chat;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import ru.flectone.Main;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FChat implements Listener {


    private FileResource locale = Main.locale;

    private FileResource config = Main.config;

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Set<Player> recipients = new HashSet<>(event.getRecipients());
        String message = event.getMessage();
        String globalPrefix = config.getString("chat.global.prefix");

        recipients.removeIf(recipient -> PlayerUtils.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString()));

        String chatType = message.startsWith(globalPrefix) && !message.equals(globalPrefix) && config.getBoolean("chat.global.enable") ? "global" : "locale";

        if (chatType.equals("locale")) {
            int localeRange = config.getInt("chat.locale.range");
            recipients.removeIf(recipient -> player.getWorld() != recipient.getWorld()
                    || player.getLocation().distance(recipient.getLocation()) > localeRange);

            if (recipients.isEmpty() && config.getBoolean("chat.no_recipients.enable")) {
                player.sendMessage(locale.getFormatString("chat.no_recipients", player));
            }
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

        itemStack = itemStack == null && message.contains("%item%") ? player.getItemInHand() : itemStack;

        for(Player recipient : recipients){

            ComponentBuilder finalBuilder = new ComponentBuilder();

            String[] localeMessage = config.getFormatString("chat." + chatType + ".message", recipient).split("<player>");


            finalBuilder.append(Utils.getNameComponent(localeMessage[0] + PlayerUtils.getPlayer(player).getName(),
                    player.getName(), player));


            finalBuilder.append(TextComponent.fromLegacyText(localeMessage[1]), ComponentBuilder.FormatRetention.NONE);

            Utils.buildMessage(message, finalBuilder, org.bukkit.ChatColor.getLastColors(localeMessage[1]), recipient, itemStack);


            recipient.spigot().sendMessage(finalBuilder.create());
        }

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

        recipients.removeIf(recipient -> PlayerUtils.getPlayer(recipient.getUniqueId()).getIgnoreList().contains(player.getUniqueId().toString()));

        createMessage(recipients, player, "%item%", "locale", event.getCursor());
    }
}
