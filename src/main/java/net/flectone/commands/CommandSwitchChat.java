package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSwitchChat extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(1)) return true;

        String chat = strings[0].toLowerCase();

        if(!chat.equals("local") && !chat.equals("global")){
            fCommand.sendUsageMessage();
            return true;
        }

        if(!Main.config.getBoolean("chat.global.enable")){
            fCommand.sendMeMessage("command.disabled");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        fCommand.getFPlayer().setChat(chat);
        fCommand.getFPlayer().setUpdated(true);

        fCommand.sendMeMessage("command.switch-chat.message", "<chat>", chat);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "local");
            isStartsWith(strings[0], "global");
        }

        return wordsList;
    }
}
