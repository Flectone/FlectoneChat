package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandBan extends FCommand {

    public CommandBan(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        if (!commands.getBoolean(getName() + ".load-minecraft-banlist")) return;

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (banList.getBanEntries().isEmpty()) return;

        Bukkit.getBannedPlayers().parallelStream().forEach(offlinePlayer -> {
            if (offlinePlayer.getName() == null) return;

            BanEntry banEntry = banList.getBanEntry(offlinePlayer.getName());
            if (banEntry == null) return;

            String source = banEntry.getSource();

            source = source.equalsIgnoreCase("console") || source.equalsIgnoreCase("plugin")
                    ? null
                    : Bukkit.getOfflinePlayer(source).getUniqueId().toString();

            String reason = banEntry.getReason() != null
                    ? banEntry.getReason()
                    : locale.getVaultString(null, this + ".default-reason");

            Moderation playerMod = new Moderation(offlinePlayer.getUniqueId().toString(), -1, reason, source, Moderation.Type.BAN);

            database.execute(() -> database.updateFPlayer("bans", playerMod));

            banList.pardon(offlinePlayer.getName());
        });

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String time = args.length == 1 ? "-1" : args[1];

        if ((!isTimeString(time) || !StringUtils.isNumeric(time.substring(0, time.length() - 1)))
                && !time.equals("-1") && !time.equals("permanent")) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String banPlayer = args[0];
        FPlayer banFPlayer = playerManager.getOffline(banPlayer);
        if (banFPlayer == null) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        if (IntegrationsModule.hasPermission(banFPlayer.getOfflinePlayer(), getPermission() + ".bypass")) {
            sendErrorMessage(commandSender, getModule() + ".player-bypass");
            return true;
        }

        int banTime = stringToTime(time);
        if (banTime < -1) {
            sendErrorMessage(commandSender, getModule() + ".long-number");
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);
        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return true;
        }

        String reason = args.length > 2
                ? MessageUtil.joinArray(args, 2, " ")
                : locale.getVaultString(commandSender, this + ".default-reason");

        String globalStringMessage = banTime == -1
                ? ".permanent-server-message"
                : ".server-message";

        String globalMessage = locale.getVaultString(commandSender, this + globalStringMessage)
                .replace("<player>", banFPlayer.getMinecraftName())
                .replace("<time>", TimeUtil.convertTime(cmdSettings.getSender(), banTime))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), globalMessage, "", false);

        String moderator = cmdSettings.getSender() != null
                ? cmdSettings.getSender().getUniqueId().toString()
                : null;

        banFPlayer.ban(reason, banTime, moderator);

        IntegrationsModule.sendDiscordBan(banFPlayer.getOfflinePlayer(), banFPlayer.getBan());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        switch (args.length) {
            case 1 -> isConfigModePlayer(args[0]);
            case 2 -> {
                isFormatString(args[1]);
                isStartsWith(args[1], "permanent");
                isStartsWith(args[1], "-1");
            }
            case 3 -> isTabCompleteMessage(commandSender, args[2], "reason");
        }

        return getSortedTabComplete();
    }
}
