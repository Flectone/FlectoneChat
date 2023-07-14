package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.integrations.voicechats.plasmovoice.FlectonePlasmoVoice;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CommandMute extends FTabCompleter {

    public CommandMute(){
        super.commandName = "mute";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(2)) return true;

        String stringTime = strings[1];

        if(!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))){
            fCommand.sendUsageMessage();
            return true;
        }

        String playerName = strings[0];
        FPlayer mutedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if(mutedFPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        String reason = strings.length > 2 ? ObjectUtil.toString(strings, 2) : Main.locale.getString("command.mute.default-reason");

        int time = fCommand.getTimeFromString(stringTime);

        String formatString = Main.locale.getString("command.mute.global-message")
                .replace("<player>", mutedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason);

        fCommand.sendGlobalMessage(new HashSet<>(Bukkit.getOnlinePlayers()), formatString, false);

        if(Main.isHavePlasmoVoice) {
            FlectonePlasmoVoice.mute(mutedFPlayer.isMuted(), mutedFPlayer.getRealName(), strings[1], reason);
        }

        mutedFPlayer.setMuteTime(time + ObjectUtil.getCurrentTime());
        mutedFPlayer.setMuteReason(reason);
        mutedFPlayer.setUpdated(true);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1) isOfflinePlayer(strings[0]);
        else if(strings.length == 2) isFormatString(strings[1]);
        else if (strings.length == 3) isStartsWith(strings[2], "(reason)");

        Collections.sort(wordsList);

        return wordsList;
    }
}
