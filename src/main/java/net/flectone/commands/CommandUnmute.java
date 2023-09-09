package net.flectone.commands;

import net.flectone.Main;
import net.flectone.integrations.voicechats.plasmovoice.FPlasmoVoice;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.entity.player.PlayerMod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandUnmute implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        PlayerMod playerMod = Main.getDatabase()
                .getPlayerInfo("mutes", "player", fPlayer.getUUID().toString());

        if (playerMod == null || playerMod.isExpired()) {
            fCommand.sendMeMessage("command.unmute.not-muted");
            return;
        }

        if (fCommand.isHaveCD()) return;

        if (HookManager.enabledPlasmoVoice) {
            FPlasmoVoice.unmute(fPlayer.getRealName());
        }

        fPlayer.unmute();

        fCommand.sendMeMessage("command.unmute.message", "<player>", fPlayer.getRealName());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            Main.getDatabase().getPlayerNameList("mutes", "player").parallelStream()
                    .forEach(playerName -> isStartsWith(strings[0], playerName));
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "unmute";
    }
}
