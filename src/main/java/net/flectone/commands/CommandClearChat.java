package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandClearChat implements FTabCompleter {

    private static final String clearedString = " \n".repeat(100);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isHaveCD()) return true;

        if (commandSender.hasPermission("flectonechat.clear-chat.other") && strings.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(clearedString));
        }

        commandSender.sendMessage(clearedString);
        fCommand.sendMeMessage("command.clear-chat.message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (!commandSender.hasPermission("flectonechat.clear-chat.other")) return wordsList;

        if (strings.length == 1) {
            isStartsWith(strings[0], "all");
        }

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "clear-chat";
    }
}
