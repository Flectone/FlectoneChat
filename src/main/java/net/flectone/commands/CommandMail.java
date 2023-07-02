package net.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.utils.ObjectUtil;

import java.util.List;

public class CommandMail implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(2)) return true;

        if(!FCommands.isOfflinePlayer(strings[0])){
            fCommand.sendMeMessage("mail.no_player");
            return true;
        }

        String playerName = strings[0];
        String message = ObjectUtil.toString(strings, 1);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer.isOnline()){
            Bukkit.dispatchCommand(commandSender, "tell " + playerName + " " + message);
            return true;
        }


        if(fCommand.isIgnored((Player) commandSender, offlinePlayer)){
            fCommand.sendMeMessage("mail.you_ignore");
            return true;
        }

        if(fCommand.isIgnored(offlinePlayer, (Player) commandSender)){
            fCommand.sendMeMessage("mail.he_ignore");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

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
