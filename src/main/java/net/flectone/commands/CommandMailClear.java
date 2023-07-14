package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.custom.Mail;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMailClear extends FTabCompleter {

    public CommandMailClear(){
        super.commandName = "mail-clear";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isConsoleMessage()) return true;

        if(fCommand.isInsufficientArgs(2)) return true;

        String playerName = strings[0];
        FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

        if(fPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        HashMap<String, Mail> mailsList = fPlayer.getMails();

        if(fPlayer.isOnline() || mailsList == null || mailsList.isEmpty()){
            fCommand.sendMeMessage("command.mail-clear.empty");
            return true;
        }

        if(!StringUtils.isNumeric(strings[1])){
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        if(fCommand.isMuted()) return true;

        int number = Integer.parseInt(strings[1]);

        Map.Entry<String, Mail> entry = mailsList.entrySet().stream()
                .filter(stringMailEntry -> !stringMailEntry.getValue().isRemoved())
                .skip(number - 1)
                .findFirst()
                .orElse(null);

        if (entry == null) {
            fCommand.sendMeMessage("command.mail-clear.wrong-number");
            return true;
        }

        String[] replaceString = {"<player>", "<message>"};
        String[] replaceTo = {fPlayer.getRealName(), entry.getValue().getMessage()};

        fPlayer.removeMail(entry.getKey());
        fPlayer.setUpdated(true);

        fCommand.sendMeMessage("command.mail-clear.message", replaceString, replaceTo);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOfflinePlayer(strings[0]);
        } else if(strings.length == 2){

            String playerName = strings[0];
            FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

            if(fPlayer != null){
                HashMap<String, Mail> mailsList = fPlayer.getMails();

                if (mailsList != null && !mailsList.isEmpty()) {
                    int[] counter = {1};
                    mailsList.entrySet().stream()
                            .filter(entry -> !entry.getValue().isRemoved())
                            .forEach(entry -> isStartsWith(strings[1], String.valueOf(counter[0]++)));
                }
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
