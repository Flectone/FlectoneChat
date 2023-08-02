package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FEntity;
import net.flectone.custom.FPlayer;
import net.flectone.custom.Mail;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        FPlayer fPlayer = FPlayerManager.addPlayer(event.getPlayer());

        event.setJoinMessage(null);
        FCommands fCommands = new FCommands(player, "join", "join", new String[]{});

        boolean sendMessage = Main.config.getBoolean("player.join.message.enable");

        if (sendMessage) {
            String string = player.hasPlayedBefore() ? Main.locale.getString("player.join.message") : Main.locale.getString("player.join.first-time.message");
            string = string.replace("<player>", player.getName());

            fCommands.sendGlobalMessage(string);
        }

        HashMap<String, Mail> mails = fPlayer.getMails();
        if (mails == null) return;

        mails.values().parallelStream().filter(mail -> !mail.isRemoved()).forEach(mail -> {

            String playerName = FPlayerManager.getPlayer(mail.getSender()).getRealName();

            String localeString = Main.locale.getFormatString("command.mail.get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", mail.getMessage());
            player.sendMessage(newLocaleString);
            mail.setRemoved(true);
        });
    }

    @EventHandler
    public void onLoginPlayer(PlayerLoginEvent event) {

        FPlayer fPlayer = FPlayerManager.getPlayer(event.getPlayer());
        if (fPlayer != null && fPlayer.isBanned()) {
            String localString = fPlayer.isPermanentlyBanned() ? "command.ban.local-message" : "command.tempban.local-message";
            int bannedTime = fPlayer.isPermanentlyBanned() ? -1 : fPlayer.getTempBanTime();

            String formatMessage = Main.locale.getFormatString(localString, fPlayer.getPlayer())
                    .replace("<time>", ObjectUtil.convertTimeToString(bannedTime))
                    .replace("<reason>", fPlayer.getBanReason());
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, formatMessage);
            return;
        }

        if (Main.config.getBoolean("command.maintenance.enable")
                && !event.getPlayer().isOp()
                && !event.getPlayer().hasPermission("flectonechat.maintenance")) {

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Main.locale.getFormatString("command.maintenance.kicked-message", null));
        }
    }
}
