package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.integrations.voicechats.plasmovoice.FlectonePlasmoVoice;
import net.flectone.managers.FPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandUnmute extends FTabCompleter {

    public CommandUnmute() {
        super.commandName = "unmute";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return true;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        if (fPlayer.getMuteTime() < 0) {
            fCommand.sendMeMessage("command.unmute.not-muted");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        if (Main.isHavePlasmoVoice) {
            FlectonePlasmoVoice.unmute(fPlayer.getRealName());
        }

        fPlayer.unmute();

        fCommand.sendMeMessage("command.unmute.message", "<player>", fPlayer.getRealName());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            FPlayerManager.getPlayers().parallelStream()
                    .filter(FPlayer::isMuted)
                    .forEach(fPlayer -> isStartsWith(strings[0], fPlayer.getRealName()));
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
