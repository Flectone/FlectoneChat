package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandMute extends FCommand {

    public CommandMute(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        if (args.length < 2) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String time = args[1];

        if (!isTimeString(time) || !StringUtils.isNumeric(time.substring(0, time.length() - 1))) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String targetPlayerName = args[0];
        FPlayer mutedFPlayer = playerManager.getOffline(targetPlayerName);

        if (mutedFPlayer == null) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        if (IntegrationsModule.hasPermission(mutedFPlayer.getOfflinePlayer(), getPermission() + ".bypass")) {
            sendErrorMessage(commandSender, getModule() + ".player-bypass");
            return true;
        }

        int muteTime = stringToTime(time);

        if (muteTime < -1) {
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

        String serverMessage = locale.getVaultString(commandSender, this + ".server-message")
                .replace("<player>", mutedFPlayer.getMinecraftName())
                .replace("<time>", TimeUtil.convertTime(cmdSettings.getSender(), muteTime))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), serverMessage, "", false);

        String moderator = cmdSettings.getSender() != null
                ? cmdSettings.getSender().getUniqueId().toString()
                : null;

        mutedFPlayer.mute(reason, muteTime, moderator);

        IntegrationsModule.sendDiscordMute(mutedFPlayer.getOfflinePlayer(), mutedFPlayer.getMute());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();
        switch (args.length) {
            case 1 -> isConfigModePlayer(args[0]);
            case 2 -> isFormatString(args[1]);
            case 3 -> isTabCompleteMessage(commandSender, args[2], "reason");
        }

        return getSortedTabComplete();
    }
}
