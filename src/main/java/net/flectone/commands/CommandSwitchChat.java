package net.flectone.commands;

import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.managers.FileManager.config;

public class CommandSwitchChat implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isConsoleMessage()
                || fCommand.isInsufficientArgs(2)) return true;

        String chatParam = strings[0].toLowerCase();
        String chat = strings[1].toLowerCase();

        if ((!chatParam.equals("switch") && !chatParam.equals("hide"))
                || (!chat.equals("local") && !chat.equals("global"))) {
            fCommand.sendUsageMessage();
            return true;
        }

        boolean isSwitch = chatParam.equals("switch");

        if (isSwitch && !config.getBoolean("chat.global.enable")) {
            fCommand.sendMeMessage("command.disabled");
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted() || fCommand.getFPlayer() == null) return true;

        String fPlayerChat = isSwitch ? chat : chat.equals("global") ? "onlylocal" : "onlyglobal";

        fCommand.getFPlayer().getChatInfo().setChatType(fPlayerChat);

        String localeString = isSwitch ? "command.switch-chat.switch-message" : "command.switch-chat.hide-message";
        fCommand.sendMeMessage(localeString, "<chat>", chat);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> {
                isStartsWith(strings[0], "switch");
                isStartsWith(strings[0], "hide");
            }
            case 2 -> {
                isStartsWith(strings[1], "local");
                isStartsWith(strings[1], "global");
            }
        }

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "switch-chat";
    }
}
