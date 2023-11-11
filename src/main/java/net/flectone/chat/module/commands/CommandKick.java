package net.flectone.chat.module.commands;

import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandKick extends FCommand {

    public CommandKick(FModule module, String name) {
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

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        if (cmdSettings.isDisabled()) {
            sendMessage(commandSender, getModule() + ".you-disabled");
            return true;
        }

        String reason = args.length == 1
                ? locale.getVaultString(commandSender, this + ".default-reason")
                : MessageUtil.joinArray(args, 1, " ");

        if (args[0].equals("@a")) {
            Bukkit.getOnlinePlayers().forEach(player -> kick(player, commandSender, cmdSettings, reason));
            return true;
        }

        Player toKick = Bukkit.getPlayer(args[0]);
        if (toKick == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        kick(toKick, commandSender, cmdSettings, reason);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        switch (args.length) {
            case 1 -> {
                isOnlinePlayer(args[0]);
                isStartsWith(args[0], "@a");
            }
            case 2 -> isTabCompleteMessage(commandSender, args[1], "reason");
        }

        return getSortedTabComplete();
    }

    public void kick(@NotNull Player toKick, @NotNull CommandSender commandSender, @NotNull CmdSettings cmdSettings, @NotNull String reason) {
        String serverMessage = locale.getVaultString(commandSender, this + ".server-message")
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());
        serverMessage = MessageUtil.formatPlayerString(toKick, serverMessage);
        serverMessage = MessageUtil.formatAll(cmdSettings.getSender(), serverMessage);

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), serverMessage, "", false);

        String playerMessage = locale.getVaultString(commandSender, this + ".player-message")
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());
        playerMessage = MessageUtil.formatPlayerString(toKick, playerMessage);
        playerMessage = MessageUtil.formatAll(cmdSettings.getSender(), playerMessage);

        toKick.kickPlayer(playerMessage);

        IntegrationsModule.sendDiscordKick(toKick, reason, commandSender.getName());
    }
}
