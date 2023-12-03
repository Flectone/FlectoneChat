package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.mail.Mail;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandClearmail extends FCommand {

    public CommandClearmail(FModule module, String name) {
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

        FlectoneChat.getPlugin().getDatabase().execute(() ->
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
            sendErrorMessage(commandSender, getModule() + ".console");
            return;
        }

        String targetPlayer = args[0];
        FPlayer fTarget = playerManager.getOffline(targetPlayer);
        if (fTarget == null) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return;
        }

        database.getMails(fTarget);

        if (fTarget.getOfflinePlayer().isOnline() || fTarget.getMailList().isEmpty()) {
            sendErrorMessage(commandSender, this + ".empty");
            return;
        }

        if (!StringUtils.isNumeric(args[1]) || Integer.parseInt(args[1]) > fTarget.getMailList().size() - 1) {
            sendErrorMessage(commandSender, getModule() + ".wrong-number");
            return;
        }

        Mail mail = fTarget.getMailList().get(Integer.parseInt(args[1]));
        if (mail == null || !mail.getSender().equals(cmdSettings.getFPlayer().getUuid().toString())) {
            sendErrorMessage(commandSender, getModule() + ".wrong-number");
            return;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return;
        }

        if (cmdSettings.isMuted()) {
            sendErrorMessage(commandSender, getModule() + ".muted");
            return;
        }

        fTarget.getMailList().remove(mail);
        database.removeMail(mail);

        String playerMessage = locale.getVaultString(commandSender, this + ".message")
                .replace("<player>", fTarget.getMinecraftName())
                .replace("<message>", mail.getMessage());

        playerMessage = MessageUtil.formatPlayerString(commandSender, playerMessage);
        playerMessage = MessageUtil.formatAll(cmdSettings.getSender(), playerMessage);

        sendFormattedMessage(commandSender, playerMessage);
        cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        if (!(commandSender instanceof Player sender)) return null;
        tabCompleteClear();

        switch (args.length) {
            case 1 -> isOfflinePlayer(args[0]);
            case 2 -> {
                String playerName = args[0];
                FPlayer fPlayer = playerManager.get(playerName);

                if (fPlayer == null) break;

                int[] counter = {0};
                fPlayer.getMailList().stream()
                        .filter(mail -> mail.getSender().equals(sender.getUniqueId().toString()))
                        .forEach(mail -> isStartsWith(args[1], String.valueOf(counter[0]++)));
            }
        }

        return getSortedTabComplete();
    }
}
