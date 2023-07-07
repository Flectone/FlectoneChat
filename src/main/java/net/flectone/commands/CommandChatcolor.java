package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandChatcolor extends FTabCompleter {

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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "default");
            isStartsWith(strings[0], "#1abaf0");
            isStartsWith(strings[0], "&b");
        } else if(strings.length == 2){
            isStartsWith(strings[1], "#77d7f7");
            isStartsWith(strings[1], "&f");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    public static String[] getDefaultColors() {
        return new String[]{Main.config.getString("color.first"), Main.config.getString("color.second")};
    }
}
