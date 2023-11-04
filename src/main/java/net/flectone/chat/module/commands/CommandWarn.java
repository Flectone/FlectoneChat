package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.locale;

public class CommandWarn extends FCommand {
    public CommandWarn(FModule module, String name) {
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

        String stringTime = args[1];

        if (!isTimeString(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        String warnedPlayerName = args[0];
        FPlayer warnedFPlayer = FPlayerManager.getOffline(warnedPlayerName);

        if (warnedFPlayer == null) {
            sendMessage(commandSender, getModule() + ".null-player");
            return;
        }

        int time = stringToTime(stringTime);

        if (time < -1) {
            sendMessage(commandSender, getModule() + ".long-number");
            return;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return;
        }

        FlectoneChat.getDatabase().getWarns(warnedFPlayer);

        String reason = args.length > 2
                ? MessageUtil.joinArray(args, 2, " ")
                : locale.getVaultString(commandSender, this + ".default-reason");

        String serverMessage = locale.getVaultString(commandSender, this + ".server-message")
                .replace("<player>", warnedFPlayer.getMinecraftName())
                .replace("<time>", TimeUtil.convertTime(cmdSettings.getSender(), time))
                .replace("<count>", String.valueOf(warnedFPlayer.getCountWarns() + 1))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), serverMessage, "", false);

        String moderator = cmdSettings.getSender() != null
                ? cmdSettings.getSender().getUniqueId().toString()
                : null;

        warnedFPlayer.warn(reason, time, moderator);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        switch (args.length) {
            case 1 -> isOfflinePlayer(args[0]);
            case 2 -> isFormatString(args[1]);
            case 3 -> isTabCompleteMessage(commandSender, args[2], "reason");
        }

        return getSortedTabComplete();
    }
}
