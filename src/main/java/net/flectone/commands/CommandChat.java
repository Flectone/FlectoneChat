package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.commands.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandChat extends FTabCompleter {

    public CommandChat() {
        super.commandName = "chat";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()) return true;

        if (fCommand.isInsufficientArgs(2)) return true;

        String chatParam = strings[0].toLowerCase();
        String chat = strings[1].toLowerCase();

        if ((!chatParam.equals("switch") && !chatParam.equals("hide")) || (!chat.equals("local") && !chat.equals("global"))) {
            fCommand.sendUsageMessage();
            return true;
        }


        boolean isSwitch = chatParam.equals("switch");

        if (isSwitch && !Main.config.getBoolean("chat.global.enable")) {
            fCommand.sendMeMessage("command.disabled");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (fCommand.isMuted()) return true;

        String fPlayerChat = isSwitch ? chat : chat.equals("global") ? "onlylocal" : "onlyglobal";

        fCommand.getFPlayer().setChat(fPlayerChat);
        fCommand.getFPlayer().setUpdated(true);

        String localeString = isSwitch ? "command.chat.switch-message" : "command.chat.hide-message";
        fCommand.sendMeMessage(localeString, "<chat>", chat);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "switch");
            isStartsWith(strings[0], "hide");
        } else if (strings.length == 2) {
            isStartsWith(strings[1], "local");
            isStartsWith(strings[1], "global");
        }

        return wordsList;
    }
}
