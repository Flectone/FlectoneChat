package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.utils.ObjectUtils;
import ru.flectone.utils.PlayerUtils;

import java.util.List;

public class CommandMail implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.checkCountArgs(2)) return true;

        if(!FCommands.isRealOfflinePlayer(strings[0])){
            fCommand.sendMeMessage("mail.no_player");
            return true;
        }

        String playerName = strings[0];
        String message = ObjectUtils.toString(strings, 1);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer.isOnline()){
            Bukkit.dispatchCommand(commandSender, "tell " + playerName + " " + message);
            return true;
        }


        if(fCommand.checkIgnoreList((Player) commandSender, offlinePlayer)){
            fCommand.sendMeMessage("mail.you_ignore");
            return true;
        }

        if(fCommand.checkIgnoreList(offlinePlayer, (Player) commandSender)){
            fCommand.sendMeMessage("mail.he_ignore");
            return true;
        }

        String key = offlinePlayer.getUniqueId() + "." + ((Player) commandSender).getUniqueId();

        List<String> mailsList = Main.mails.getStringList(key);
        mailsList.add(message);

        Main.mails.set(key, mailsList);
        Main.mails.saveFile();

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {offlinePlayer.getName(), message};

        fCommand.sendMeMessage("mail.success_send", replaceString, replaceTo);

        return true;
    }
}
