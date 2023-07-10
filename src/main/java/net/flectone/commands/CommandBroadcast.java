package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CommandBroadcast extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(fCommand.isHaveCD() || fCommand.isMuted()) return true;

        String formatString = Main.locale.getString("command.broadcast.message")
                .replace("<player>", fCommand.getSenderName())
                .replace("<message>", ObjectUtil.toString(strings));

        fCommand.sendGlobalMessage(new HashSet<>(Bukkit.getOnlinePlayers()), formatString,  ObjectUtil.toString(strings), null, false);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "(message)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
