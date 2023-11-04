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

import static net.flectone.chat.manager.FileManager.locale;

public class CommandBall extends FCommand {

    public CommandBall(FModule module, String name) {
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

        CmdSettings cmdSettings = processCommand(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        if (cmdSettings.isMuted()) {
            cmdSettings.getFPlayer().sendMutedMessage();
            return true;
        }

        if (cmdSettings.isDisabled()) {
            sendMessage(commandSender, getModule() + ".you-disabled");
            return true;
        }

        List<String> answers = locale.getVaultStringList(commandSender, this + ".answers");
        if (answers.isEmpty()) return true;

        int randomPer = RandomUtil.nextInt(0, answers.size());

        String formatString = locale.getVaultString(commandSender, this + ".message");
        formatString = MessageUtil.formatPlayerString(commandSender, formatString)
                .replace("<answer>", answers.get(randomPer));

        String message = String.join(" ", args);

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), formatString, message, true);
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();

        if (args.length == 1) {
            isTabCompleteMessage(commandSender, args[0], "message");
        }

        return getSortedTabComplete();
    }
}
