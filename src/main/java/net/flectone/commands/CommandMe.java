package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.utils.ObjectUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandMe implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(1)) return true;

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        String formatString = Main.locale.getString("me.message")
                .replace("<player>", fCommand.getSenderName())
                .replace("<message>", ObjectUtils.toString(strings));

        fCommand.sendGlobalMessage(formatString);

        return true;
    }
}
