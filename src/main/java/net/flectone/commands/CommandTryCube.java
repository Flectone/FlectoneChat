package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandTryCube implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return true;

        if (!StringUtils.isNumeric(strings[0])) {
            fCommand.sendMeMessage("command.try-cube.only-number");
            return true;
        }

        int amount = Integer.parseInt(strings[0]);

        if (amount > config.getInt("command.try-cube.max-amount") || amount == 0) {
            fCommand.sendMeMessage("command.try-cube.too-much");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        int values = 0;

        while (amount-- != 0) {
            int cubeType = ObjectUtil.nextInt(6) + 1;
            values += cubeType;
            stringBuilder.append(locale.getString("command.try-cube.format." + cubeType)).append(" ");
        }

        String formatString = locale.getString("command.try-cube." + (values >= Integer.parseInt(strings[0]) * 3.5) + "-message")
                .replace("<cube>", stringBuilder.toString())
                .replace("<player>", fCommand.getSenderName());

        if (amount == 6 && values == 21 && stringBuilder.toString().equals("⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ")) {
            formatString = locale.getString("command.try-cube.lucky-message")
                    .replace("<player>", fCommand.getSenderName());
        }

        fCommand.sendGlobalMessage(formatString, "", null, true);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isDigitInArray(strings[0], "", 1, config.getInt("command.try-cube.max-amount") + 1);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "try-cube";
    }
}
