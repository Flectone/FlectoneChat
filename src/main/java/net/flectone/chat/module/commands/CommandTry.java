package net.flectone.chat.module.commands;

import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.RandomUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.commands;
import static net.flectone.chat.manager.FileManager.locale;

public class CommandTry extends FCommand {
    public CommandTry(FModule module, String name) {
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
            sendCDMessage(cmdSettings.getSender(), alias, cmdSettings.getCooldownTime());
            return true;
        }

        if (cmdSettings.isMuted()) {
            sendMutedMessage(cmdSettings.getFPlayer());
            return true;
        }

        if (cmdSettings.isDisabled()) {
            sendMessage(commandSender, getModule() + ".you-disabled");
            return true;
        }

        int min = commands.getInt(getName() + ".min-%");
        int max = commands.getInt(getName() + ".max-%");
        int good = commands.getInt(getName() + ".good-%");

        int randomPer = RandomUtil.nextInt(min, max);

        String formatString = locale.getVaultString(cmdSettings.getSender(), this + "." + (randomPer >= good) + "-message")
                .replace("<percent>", String.valueOf(randomPer));

        formatString = MessageUtil.formatPlayerString(commandSender, formatString);

        String message = MessageUtil.joinArray(args, 0, " ");

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), formatString, message, true);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isTabCompleteMessage(commandSender, args[0], "message");
        }

        return getTAB_COMPLETE();
    }
}
