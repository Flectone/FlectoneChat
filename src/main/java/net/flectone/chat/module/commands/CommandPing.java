package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandPing extends FCommand {

    private static final String CONFIG_PING_PATH = "player-message.formatting.list.ping";

    public CommandPing(FModule module, String name) {
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


        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        FPlayer fPlayer = args.length == 0
                ? cmdSettings.getFPlayer()
                : playerManager.get(args[0]);

        if (fPlayer == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return true;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        int ping = fPlayer.getPlayer().getPing();
        int badPing = config.getVaultInt(commandSender, CONFIG_PING_PATH + ".bad.count");
        int mediumPing = config.getVaultInt(commandSender, CONFIG_PING_PATH + ".medium.count");

        String pingColor;
        if (ping > badPing) {
            pingColor = config.getVaultString(commandSender, CONFIG_PING_PATH + ".bad.color");
        } else if (ping > mediumPing) {
            pingColor = config.getVaultString(commandSender, CONFIG_PING_PATH + ".medium.color");
        } else {
            pingColor = config.getVaultString(commandSender, CONFIG_PING_PATH + ".good.color");
        }

        pingColor = MessageUtil.formatAll(cmdSettings.getSender(), pingColor) + ping;

        if (commandSender.equals(fPlayer.getPlayer())) {
            String message = locale.getVaultString(commandSender, this + ".myself-message")
                    .replace("<ping>", pingColor);

            commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), message));
            return true;
        }

        String message = locale.getVaultString(commandSender, this + ".message")
                .replace("<ping>", pingColor);

        message = MessageUtil.formatPlayerString(fPlayer.getPlayer(), message);

        commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), fPlayer.getPlayer(), message));

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
            isOnlinePlayer(args[0]);
        }

        return getSortedTabComplete();
    }
}
