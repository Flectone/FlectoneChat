package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
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

import static net.flectone.chat.manager.FileManager.locale;

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

        String targetPlayer = args[0];
        FPlayer fTarget = FPlayerManager.getOffline(targetPlayer);
        if (fTarget == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return;
        }

        FlectoneChat.getDatabase().getMails(fTarget);

        if (fTarget.getOfflinePlayer().isOnline() || fTarget.getMailList().isEmpty()) {
            sendMessage(commandSender, this + ".empty");
            return;
        }

        if (!StringUtils.isNumeric(args[1]) || Integer.parseInt(args[1]) > fTarget.getMailList().size() - 1) {
            sendMessage(commandSender, getModule() + ".wrong-number");
            return;
        }

        Mail mail = fTarget.getMailList().get(Integer.parseInt(args[1]));
        if (mail == null || !mail.getSender().equals(cmdSettings.getFPlayer().getUuid().toString())) {
            sendMessage(commandSender, getModule() + ".wrong-number");
            return;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return;
        }

        if (cmdSettings.isMuted()) {
            sendMessage(commandSender, getModule() + ".muted");
            return;
        }

        fTarget.getMailList().remove(mail);
        FlectoneChat.getDatabase().removeMail(mail);

        String playerMessage = locale.getVaultString(commandSender, this + ".message")
                .replace("<player>", fTarget.getMinecraftName())
                .replace("<message>", mail.getMessage());

        playerMessage = MessageUtil.formatPlayerString(commandSender, playerMessage);
        playerMessage = MessageUtil.formatAll(cmdSettings.getSender(), playerMessage);

        commandSender.sendMessage(playerMessage);
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
                FPlayer fPlayer = FPlayerManager.get(playerName);

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
