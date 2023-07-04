package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinPlayer(PlayerJoinEvent event){
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        new FPlayer(player);

        event.setJoinMessage(null);
        FCommands fCommands = new FCommands(player, "join", "join", new String[]{});

        String string = player.hasPlayedBefore() ? Main.locale.getString("join.message") : Main.locale.getString("join.first_time.message");
        string = string.replace("<player>", player.getName());

        fCommands.sendGlobalMessage(string);

        Set<String> keysList = Main.mails.getKeys()
                .stream()
                .filter(keys -> keys.startsWith(player.getUniqueId() + "."))
                .map(String::valueOf).collect(Collectors.toSet());

        keysList.forEach(key -> {
            List<String> mailsList = Main.mails.getStringList(key);

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(key.replace(player.getUniqueId() + ".", "")));

            String localeString = Main.locale.getFormatString("mail.success_get", player)
                    .replace("<player>", offlinePlayer.getName());

            mailsList.forEach(message -> {
                String newLocaleString = localeString.replace("<message>", message);

                player.sendMessage(newLocaleString);
            });

            Main.mails.updateFile(key, null);
        });
    }
}
