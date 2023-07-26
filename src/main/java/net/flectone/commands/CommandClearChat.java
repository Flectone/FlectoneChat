package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandClearChat extends FTabCompleter {

    public CommandClearChat() {
        super.commandName = "clear-chat";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isHaveCD()) return true;

        String string = " \n".repeat(100);

        commandSender.sendMessage(string);
        fCommand.sendMeMessage("command.clear-chat.message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return wordsList;
    }
}
