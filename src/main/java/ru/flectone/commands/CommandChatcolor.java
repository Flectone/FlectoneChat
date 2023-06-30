package ru.flectone.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;

public class CommandChatcolor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(strings.length == 0 || (strings.length != 2 && !strings[0].equalsIgnoreCase("default"))) {
            fCommand.sendUsageMessage();
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(strings[0].equalsIgnoreCase("default")){
            strings = getDefaultColors();
        }

        fCommand.getFPlayer().setColors(strings[0], strings[1]);

        fCommand.sendMeMessage("chatcolor.message");
        return true;
    }

    private String[] getDefaultColors() {
        return new String[]{Main.config.getString("color.first"), Main.config.getString("color.second")};
    }
}
