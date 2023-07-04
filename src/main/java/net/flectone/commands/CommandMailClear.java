package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMailClear extends FTabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];

        if(!FCommands.isOfflinePlayer(playerName)){
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

        if(!StringUtils.isNumeric(strings[1]) || Integer.parseInt(strings[1]) > (mailsList.size())){
            fCommand.sendMeMessage("mail-clear.not_number");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        int number = Integer.parseInt(strings[1]) - 1;

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {offlinePlayer.getName(), mailsList.get(number)};

        mailsList.remove(number);

        Main.mails.updateFile(key, mailsList);

        fCommand.sendMeMessage("mail-clear.success", replaceString, replaceTo);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOfflinePlayer(strings[0]);
        } else if(commandSender instanceof Player && strings.length == 2 && FCommands.isOfflinePlayer(strings[0])){
            String key = Bukkit.getOfflinePlayer(strings[0]).getUniqueId() + "." + ((Player) commandSender).getPlayer().getUniqueId();
            List<String> list = Main.mails.getStringList(key);
            for(int x = 0; x < list.size(); x++){
                isStartsWith(strings[1], String.valueOf(x + 1));
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
