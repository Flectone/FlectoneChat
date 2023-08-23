package net.flectone.listeners;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.actions.Mail;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.entity.FEntity;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class PlayerJoinListener implements Listener {

    public static void sendJoinMessage(@NotNull FPlayer fPlayer, @NotNull Player player, boolean isOnline) {
        boolean isEnable = config.getBoolean("player.join.message.enable");
        if (isEnable && isOnline) {
            FCommand fCommand = new FCommand(player, "join", "join", new String[]{});

            String string = player.hasPlayedBefore()
                    ? locale.getString("player.join.message")
                    : locale.getString("player.join.first-time.message");
            string = string.replace("<player>", player.getName());

            fCommand.sendGlobalMessage(string);
        }

        HashMap<UUID, Mail> mails = fPlayer.getMails();
        if (mails.isEmpty()) return;

        mails.values().parallelStream().filter(mail -> !mail.isRemoved()).forEach(mail -> {
            FPlayer mailFPlayer = FPlayerManager.getPlayer(mail.getSender());
            if (mailFPlayer == null) return;

            String playerName = mailFPlayer.getRealName();

            String localeString = locale.getFormatString("command.mail.get", player)
                    .replace("<player>", playerName);

            String newLocaleString = localeString.replace("<message>", mail.getMessage());
            player.sendMessage(newLocaleString);
            mail.setRemoved(true);
        });
    }

    public static void sendJoinMessage(@NotNull Player player) {
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if (fPlayer == null) return;
        sendJoinMessage(fPlayer, player, true);
    }

    @EventHandler
    public void joinPlayer(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        FEntity.removeBugEntities(player);

        event.setJoinMessage(null);

        FPlayer fPlayer = FPlayerManager.addPlayer(event.getPlayer());
        if (fPlayer == null) return;
        sendJoinMessage(fPlayer, player, fPlayer.isOnline());
    }

    @EventHandler
    public void onLoginPlayer(@NotNull PlayerLoginEvent event) {

        FPlayer fPlayer = FPlayerManager.getPlayer(event.getPlayer());
        if (fPlayer != null && fPlayer.isBanned()) {
            String localString = fPlayer.isPermanentlyBanned() ? "command.ban.local-message" : "command.tempban.local-message";
            int bannedTime = fPlayer.isPermanentlyBanned() ? -1 : fPlayer.getTempBanTime();

            String formatMessage = locale.getFormatString(localString, fPlayer.getPlayer())
                    .replace("<time>", ObjectUtil.convertTimeToString(bannedTime))
                    .replace("<reason>", fPlayer.getBanReason());
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, formatMessage);
            return;
        }

        if (config.getBoolean("command.maintenance.turn-on")
                && !event.getPlayer().isOp()
                && !event.getPlayer().hasPermission("flectonechat.maintenance")) {

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, locale.getFormatString("command.maintenance.kicked-message", null));
        }
    }
}
