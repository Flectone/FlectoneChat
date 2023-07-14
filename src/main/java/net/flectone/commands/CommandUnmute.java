package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.integrations.voicechats.plasmovoice.FlectonePlasmoVoice;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandUnmute extends FTabCompleter {

    public CommandUnmute(){
        super.commandName = "unmute";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if(fPlayer == null){
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if(fPlayer.getMuteTime() < 0){
            fCommand.sendMeMessage("command.unmute.not-muted");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        fPlayer.setMuteTime(0);
        fPlayer.setMuteReason("");
        fPlayer.setUpdated(true);

        fCommand.sendMeMessage("command.unmute.message", "<player>", fPlayer.getRealName());

        if(Main.isHavePlasmoVoice) {
            FlectonePlasmoVoice.unmute(fPlayer.getRealName());
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            FPlayerManager.getPlayers().stream().filter(FPlayer::isMuted).forEach(fPlayer ->
                    isStartsWith(strings[0], fPlayer.getRealName()));
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
