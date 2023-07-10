package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.custom.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandTempban extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(2)) return true;

        String stringTime = strings[1];

        if((!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1)))
                && !stringTime.equals("permanent")){
            fCommand.sendUsageMessage();
            return true;
        }

        String playerName = strings[0];
        FPlayer bannedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if(bannedFPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        String reason = strings.length > 2 ? ObjectUtil.toString(strings, 2) : Main.locale.getString("command.tempban.default-reason");

        int time = fCommand.getTimeFromString(stringTime);

        String globalStringMessage = time == -1 ? "command.ban.global-message" : "command.tempban.global-message";

        String globalMessage = Main.locale.getString(globalStringMessage)
                .replace("<player>", bannedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        fCommand.sendGlobalMessage(globalMessage, false);

        bannedFPlayer.setTempBanTime(time == -1 ? -1 : time + ObjectUtil.getCurrentTime());
        bannedFPlayer.setTempBanReason(reason);
        bannedFPlayer.setUpdated(true);

        if(!bannedFPlayer.isOnline()) return true;

        String localStringMessage = time == -1 ? "command.ban.local-message" : "command.tempban.local-message";

        String localMessage = Main.locale.getFormatString(localStringMessage, bannedFPlayer.getPlayer())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        bannedFPlayer.getPlayer().kickPlayer(localMessage);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOfflinePlayer(strings[0]);

        } else if(strings.length == 2){
            for(String format : FCommands.formatTimeList){
                if(strings[1].length() != 0 && StringUtils.isNumeric(strings[1].substring(strings[1].length() - 1))){
                    isStartsWith(strings[1], strings[1] + format);
                } else {
                    for(int x = 1; x < 10; x++){
                        isStartsWith(strings[1], x + format);
                    }
                }
            }
            isStartsWith(strings[1], "permanent");
        } else if (strings.length == 3){
            isStartsWith(strings[2], "(reason)");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
