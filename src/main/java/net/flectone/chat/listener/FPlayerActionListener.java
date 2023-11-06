package net.flectone.chat.listener;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.mail.Mail;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.flectone.chat.manager.FileManager.locale;

public class FPlayerActionListener extends FListener {

    public FPlayerActionListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoinEvent(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = new FPlayer(player);
        fPlayer.init();

        for (Mail mail : fPlayer.getMailList()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(mail.getSender()));
            if (offlinePlayer.getName() == null) continue;

            String sendMessage = locale.getVaultString(player,  "commands.mail.get")
                    .replace("<player>", offlinePlayer.getName())
                    .replace("<message>", mail.getMessage());

            sendMessage = MessageUtil.formatAll(player, sendMessage);

            player.sendMessage(sendMessage);

            FlectoneChat.getDatabase().removeMail(mail);
        }

        fPlayer.getMailList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(@NotNull PlayerQuitEvent event) {
        FPlayer fPlayer = FPlayerManager.get(event.getPlayer());
        if (fPlayer == null) return;
        fPlayer.terminate();
    }

    @EventHandler
    public void onLoginPlayer(@NotNull AsyncPlayerPreLoginEvent event) {

        if (!FPlayerManager.getBANNED_PLAYERS().contains(event.getUniqueId())) return;

        String uuidString = event.getUniqueId().toString();
        Moderation moderation = FlectoneChat.getDatabase()
                    .getPlayerInfo("bans", "player", uuidString, Moderation.Type.BAN);

        if (moderation == null || moderation.isExpired()) return;

        String localString = moderation.getTime() == -1
                ? "commands.ban.permanent-player-message"
                : "commands.ban.player-message";

        String formatMessage = locale.getVaultString(null, localString)
                .replace("<time>", TimeUtil.convertTime(null, moderation.getRemainingTime()))
                .replace("<reason>", moderation.getReason())
                .replace("<moderator>", moderation.getModeratorName());

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, MessageUtil.formatAll(null, formatMessage));
    }
}
