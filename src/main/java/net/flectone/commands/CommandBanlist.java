package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandBanlist extends FTabCompleter {

    public CommandBanlist(){
        super.commandName = "banlist";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isHaveCD()) return true;

        int perpage = 20;

        int lastPage = (int) Math.ceil((double) FPlayerManager.getBannedPlayers().size() / perpage);

        int page = strings.length > 0 ? Math.max(0, Integer.parseInt(strings[0])) : 1;

        page = Math.min(lastPage, page);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("page: ").append(page).append("/").append(lastPage);
        FPlayerManager.getBannedPlayers().stream().skip((long) (page - 1) * perpage).limit(perpage).forEach(fPlayer -> {
            stringBuilder.append("\n").append(fPlayer.getRealName());
        });

        commandSender.sendMessage(stringBuilder.toString());

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
