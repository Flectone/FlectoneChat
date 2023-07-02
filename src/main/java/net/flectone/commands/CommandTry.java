package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;

import java.util.Random;

public class CommandTry implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.checkCountArgs(1)) return true;

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        Random random = new Random();
        int randomPer = random.nextInt(100);
        randomPer += 1;

        String formatString = Main.locale.getString("try.success_" + (randomPer >= 50))
                .replace("<player>", fCommand.getSenderName())
                .replace("<percent>", String.valueOf(randomPer));

        fCommand.sendGlobalMessage(formatString, ObjectUtil.toString(strings));

        return true;
    }
}
