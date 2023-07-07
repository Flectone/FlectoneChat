package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import net.flectone.custom.Mail;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinPlayer(PlayerJoinEvent event){
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        FPlayer fPlayer = FPlayerManager.addPlayer(event.getPlayer());

        event.setJoinMessage(null);
        FCommands fCommands = new FCommands(player, "join", "join", new String[]{});

        String string = player.hasPlayedBefore() ? Main.locale.getString("join.message") : Main.locale.getString("join.first_time.message");
        string = string.replace("<player>", player.getName());

        fCommands.sendGlobalMessage(string);

        HashMap<String, Mail> mails = fPlayer.getMails();
        if(mails == null) return;

        mails.values().stream().filter(mail -> !mail.isRemoved()).forEach(mail -> {

            String playerName = FPlayerManager.getPlayer(mail.getSender()).getRealName();

            String localeString = Main.locale.getFormatString("mail.success_get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", mail.getMessage());
            player.sendMessage(newLocaleString);
            mail.setRemoved(true);
        });
    }
}
