package net.flectone.chat.module.commands;

import net.flectone.chat.model.mail.Mail;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandMail extends FCommand {

    public CommandMail(FModule module, String name) {
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

        database.execute(() -> asyncOnCommand(commandSender, command, alias, args));

        return true;
    }

    public void asyncOnCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                               @NotNull String[] args) {

        if (args.length < 2) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendErrorMessage(commandSender, getModule() + ".console");
            return;
        }

        String targetPlayer = args[0];
        FPlayer fTarget = playerManager.getOffline(targetPlayer);
        if (fTarget == null) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return;
        }

        if (cmdSettings.isDisabled()) {
            sendErrorMessage(commandSender, getModule() + ".you-disabled");
            return;
        }

        database.getSettings(fTarget);
        String value = fTarget.getSettings().getValue(Settings.Type.COMMAND_MAIL);

        if (value != null && value.equals("-1")) {
            sendErrorMessage(commandSender, getModule() + ".he-disabled");
            return;
        }

        FPlayer fSender = cmdSettings.getFPlayer();

        if (fSender.getIgnoreList().contains(fTarget.getUuid())) {
            sendErrorMessage(commandSender, getModule() + ".you-ignore");
            return;
        }

        database.getIgnores(fTarget);

        if (fTarget.getIgnoreList().contains(fSender.getUuid())) {
            sendErrorMessage(commandSender, getModule() + ".he-ignore");
            return;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return;
        }

        if (cmdSettings.isMuted()) {
            fSender.sendMutedMessage(command.getName());
            return;
        }

        String message = MessageUtil.joinArray(args, 1, " ");

        if (fTarget.getOfflinePlayer().isOnline()) {
            dispatchCommand(commandSender, "tell " + targetPlayer + " " + message);
            return;
        }

        Mail mail = new Mail(fSender.getUuid().toString(), fTarget.getUuid().toString(), message);
        fTarget.getMailList().add(mail);

        database.addMail(mail);

        String sendMessage = locale.getVaultString(commandSender, this + ".send")
                .replace("<player>", fTarget.getMinecraftName())
                .replace("<message>", message);

        sendMessage = MessageUtil.formatAll(cmdSettings.getSender(), sendMessage);

        sendFormattedMessage(commandSender, sendMessage);

        cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        switch (args.length) {
            case 1 -> isOfflinePlayer(args[0]);
            case 2 -> isTabCompleteMessage(commandSender, args[1], "message");
        }

        return getSortedTabComplete();
    }
}
