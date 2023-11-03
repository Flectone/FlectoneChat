package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
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

import static net.flectone.chat.manager.FileManager.locale;

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

        FlectoneChat.getDatabase().execute(() ->
                asyncOnCommand(commandSender, command, alias, args));

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
            sendMessage(commandSender, getModule() + ".console");
            return;
        }

        String targetPlayer = args[0];
        FPlayer fTarget = FPlayerManager.getOffline(targetPlayer);
        if (fTarget == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return;
        }

        if (cmdSettings.isDisabled()) {
            sendMessage(commandSender, getModule() + ".you-disabled");
            return;
        }

        FlectoneChat.getDatabase().getSettings(fTarget);
        String value = fTarget.getSettings().getValue(Settings.Type.COMMAND_MAIL);

        if (value != null && value.equals("-1")) {
            sendMessage(commandSender, getModule() + ".he-disabled");
            return;
        }

        FPlayer fSender = cmdSettings.getFPlayer();

        if (fSender.getIgnoreList().contains(fTarget.getUuid())) {
            sendMessage(commandSender, getModule() + ".you-ignore");
            return;
        }

        FlectoneChat.getDatabase().getIgnores(fTarget);

        if (fTarget.getIgnoreList().contains(fSender.getUuid())) {
            sendMessage(commandSender, getModule() + ".he-ignore");
            return;
        }

        if (cmdSettings.isHaveCooldown()) {
            sendCDMessage(cmdSettings.getSender(), alias, cmdSettings.getCooldownTime());
            return;
        }

        if (cmdSettings.isMuted()) {
            sendMutedMessage(fSender);
            return;
        }

        String message = MessageUtil.joinArray(args, 1, " ");

        if (fTarget.getOfflinePlayer().isOnline()) {
            dispatchCommand(commandSender, "tell " + targetPlayer + " " + message);
            return;
        }

        Mail mail = new Mail(fSender.getUuid().toString(), fTarget.getUuid().toString(), message);
        fTarget.getMailList().add(mail);

        FlectoneChat.getDatabase().addMail(mail);

        String sendMessage = locale.getVaultString(commandSender, this + ".send")
                .replace("<player>", fTarget.getMinecraftName())
                .replace("<message>", message);

        sendMessage = MessageUtil.formatAll(cmdSettings.getSender(), sendMessage);

        commandSender.sendMessage(sendMessage);
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
