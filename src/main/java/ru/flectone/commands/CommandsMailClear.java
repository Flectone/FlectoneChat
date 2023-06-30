package ru.flectone.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;

import java.util.List;

public class CommandsMailClear implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.checkCountArgs(2)) return true;

        String playerName = strings[0];

        if(!FCommands.isRealOfflinePlayer(playerName)){
            fCommand.sendMeMessage("mail-clear.no_player");
            return true;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String key = offlinePlayer.getUniqueId() + "." + ((Player) commandSender).getUniqueId();

        List<String> mailsList = Main.mails.getStringList(key);

        if(offlinePlayer.isOnline() || mailsList.isEmpty()){
            fCommand.sendMeMessage("mail-clear.empty");
            return true;
        }

        if(!StringUtils.isNumeric(strings[1]) || Integer.parseInt(strings[1]) > (mailsList.size() + 1)){
            fCommand.sendMeMessage("mail-clear.not_number");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        int number = Integer.parseInt(strings[1]) - 1;

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {offlinePlayer.getName(), mailsList.get(number)};

        mailsList.remove(number);
        Main.mails.set(key, mailsList);
        Main.mails.saveFile();

        fCommand.sendMeMessage("mail-clear.success", replaceString, replaceTo);

        return true;
    }
}
