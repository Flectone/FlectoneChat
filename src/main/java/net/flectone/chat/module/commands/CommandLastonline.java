package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandLastonline extends FCommand {

    public CommandLastonline(FModule module, String name) {
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

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String targetPlayer = args[0];
        FPlayer fTarget = playerManager.getOffline(targetPlayer);
        if (fTarget == null) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return true;
        }

        if (fTarget.getOfflinePlayer().isOnline()) {
            String message = locale.getVaultString(commandSender, this + ".currently");
            message = MessageUtil.formatPlayerString(fTarget.getPlayer(), message);
            message = MessageUtil.formatAll(cmdSettings.getSender(), message);
            commandSender.sendMessage(message);
            return true;
        }

        OfflinePlayer player = fTarget.getOfflinePlayer();

        String message = locale.getVaultString(commandSender, this + ".message")
                .replace("<player>", targetPlayer)
                .replace("<time>", TimeUtil.convertTime(cmdSettings.getSender(), player.getLastPlayed()));

        message = IntegrationsModule.setPlaceholders(player, player, message);
        message = MessageUtil.formatAll(cmdSettings.getSender(), message);

        sendFormattedMessage(commandSender, message);

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isOfflinePlayer(args[0]);
        }

        return getSortedTabComplete();
    }
}
