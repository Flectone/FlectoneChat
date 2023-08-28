package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.brand.ServerBrand;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FEntity;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.info.ModInfo;
import net.flectone.utils.ObjectUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class PlayerJoinListener implements Listener {

    public static void sendJoinMessage(@NotNull FPlayer fPlayer, @NotNull Player player, boolean isOnline) {
        boolean isEnable = config.getBoolean("player.join.message.enable");

        if(!isEnable || !isOnline) return;

        String string = player.hasPlayedBefore()
                ? locale.getString("player.join.message")
                : locale.getString("player.join.first-time.message");
        string = string.replace("<player>", player.getName());

        FCommand fCommand = new FCommand(player, "join", "join", string.split(" "));

        fCommand.sendGlobalMessage(string, "", null, true);
    }

    public static void sendJoinMessage(@NotNull Player player) {
        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if (fPlayer == null) return;
        sendJoinMessage(fPlayer, player, true);
    }

    @EventHandler
    public void joinPlayer(@NotNull PlayerJoinEvent event) {
        if (config.getBoolean("server.brand.enable"))
            ServerBrand.getInstance().setBrand(event.getPlayer());

        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            FTabCompleter.offlinePlayerList.add(player.getName());
            FPlayerManager.getUsedFPlayers().remove(player.getName());
        }

        FEntity.removeBugEntities(player);

        event.setJoinMessage(null);

        FPlayer fPlayer = FPlayerManager.createFPlayer(event.getPlayer());

        Main.getDataThreadPool().execute(() -> {
            fPlayer.synchronizeDatabase();
            sendJoinMessage(fPlayer, player, fPlayer.isOnline());
        });
    }

    @EventHandler
    public void onLoginPlayer(@NotNull PlayerLoginEvent event) {
        if(!event.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) return;

        ModInfo modInfo = Main.getDatabase()
                .getPlayerInfo("bans", "player", event.getPlayer().getUniqueId().toString());

        if (modInfo != null) {
            String localString = modInfo.getTime() == -1 ? "command.ban.local-message" : "command.tempban.local-message";

            String formatMessage = locale.getFormatString(localString, event.getPlayer())
                    .replace("<time>", ObjectUtil.convertTimeToString(modInfo.getDifferenceTime()))
                    .replace("<reason>", modInfo.getReason())
                    .replace("<moderator>", modInfo.getModeratorName());
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
