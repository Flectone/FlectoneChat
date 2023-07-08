package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import net.flectone.custom.Mail;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinPlayer(PlayerJoinEvent event){
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        FPlayer fPlayer = FPlayerManager.addPlayer(event.getPlayer());

        event.setJoinMessage(null);
        FCommands fCommands = new FCommands(player, "join", "join", new String[]{});

        String string = player.hasPlayedBefore() ? Main.locale.getString("player.join.message") : Main.locale.getString("player.join.first-time.message");
        string = string.replace("<player>", player.getName());

        fCommands.sendGlobalMessage(string);

        HashMap<String, Mail> mails = fPlayer.getMails();
        if(mails == null) return;

        mails.values().stream().filter(mail -> !mail.isRemoved()).forEach(mail -> {

            String playerName = FPlayerManager.getPlayer(mail.getSender()).getRealName();

            String localeString = Main.locale.getFormatString("command.mail.get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", mail.getMessage());
            player.sendMessage(newLocaleString);
            mail.setRemoved(true);
        });
    }

    @EventHandler
    public void onLoginPlayer(PlayerLoginEvent event){
        if(Main.config.getBoolean("command.technical-works.enable")
                && !event.getPlayer().isOp()
                && !event.getPlayer().hasPermission("flectonechat.technical-works")){

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Main.locale.getFormatString("command.technical-works.kicked-message", null));
        }
    }
}
