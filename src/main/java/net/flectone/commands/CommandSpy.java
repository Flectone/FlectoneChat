package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSpy implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);
        FPlayer fPlayer = fCommand.getFPlayer();

        if (fCommand.isConsoleMessage() || fPlayer == null) return true;

        boolean isSpies = !fPlayer.isSpies();
        fPlayer.setSpies(isSpies);

        Main.getDataThreadPool().execute(() -> Main.getDatabase().updateFPlayer(fPlayer, "spy"));

        fCommand.sendMeMessage("command.spy.message-" + isSpies);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();
        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "spy";
    }
}
