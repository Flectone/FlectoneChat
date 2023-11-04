package net.flectone.chat.module.commands;

import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.commands;
import static net.flectone.chat.manager.FileManager.locale;

public class CommandDice extends FCommand {

    private final int min = commands.getInt(getName() + ".min");
    private final int max = commands.getInt(getName() + ".max");
    private final double winCoef = commands.getDouble(getName() + ".win-coefficient");


    public CommandDice(FModule module, String name) {
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

        if (!StringUtils.isNumeric(args[0])) {
            sendMessage(commandSender, this + ".only-number");
            return true;
        }

        int amount = Integer.parseInt(args[0]);

        if (amount > max || amount < min) {
            sendMessage(commandSender, getModule() + ".wrong-number");
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

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

        StringBuilder stringBuilder = new StringBuilder();
        int sum = 0;

        while (amount-- != 0) {
            int cubeType = RandomUtil.nextInt(min, max);
            sum += cubeType;

            stringBuilder
                    .append(locale.getVaultString(commandSender, this + ".format." + cubeType))
                    .append(" ");
        }

        String formatString = locale.getVaultString(commandSender, this + "." + (sum >= Integer.parseInt(args[0]) * winCoef) + "-message")
                .replace("<cube>", stringBuilder.toString());

        formatString = MessageUtil.formatPlayerString(commandSender, formatString);

        String message = MessageUtil.joinArray(args, 1, " ");

        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), formatString, message, true);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isDigitInArray(args[0], "", min, max + 1);
        }

        return getSortedTabComplete();
    }
}
