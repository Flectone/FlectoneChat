package ru.flectone.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.utils.ObjectUtils;

public class CommandMe implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(1)) return true;

        String formatString = Main.locale.getString("me.message")
                .replace("<player>", fCommand.getSenderName())
                .replace("<message>", ObjectUtils.toString(strings));

        fCommand.sendGlobalMessage(formatString);

        return true;
    }
}
