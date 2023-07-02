package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandMute implements CommandExecutor {
    public final static String[] formatTimeList = {"s", "m", "h", "d", "y"};

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(2)) return true;

        String stringTime = strings[1];

        if(!isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))){
            fCommand.sendUsageMessage();
            return true;
        }

        if(!FCommands.isOfflinePlayer(strings[0])){
            fCommand.sendMeMessage("mute.no_player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        OfflinePlayer mutedPlayer = Bukkit.getOfflinePlayer(strings[0]);

        String reason = strings.length > 2 ? ObjectUtil.toString(strings, 2) : Main.locale.getString("mute.reason.default");

        int time = getTimeFromString(stringTime);

        List<String> muteList = new ArrayList<>();
        muteList.add(reason);
        muteList.add(String.valueOf(time + ObjectUtil.getCurrentTime()));

        Main.mutes.set(mutedPlayer.getUniqueId().toString(), muteList);
        Main.mutes.saveFile();

        String formatString = Main.locale.getString("mute.success_send")
                .replace("<player>", mutedPlayer.getName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        fCommand.sendGlobalMessage(formatString, false);

        return true;
    }

    private boolean isStringTime(String string){

        for(String format : formatTimeList){
            if(string.contains(format)) return true;
        }

        return false;
    }

    private int getTimeFromString(String string){
        int time = Integer.parseInt(string.substring(0, string.length() - 1));
        string = string.substring(string.length() - 1);

        switch (string){
            case "y": time *= 30 * 12;
            case "d": time *= 24;
            case "h": time *= 60;
            case "m": time *= 60;
            case "s": break;
        }

        return time;
    }
}
