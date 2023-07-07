package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.custom.FTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandPing extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(commandSender instanceof ConsoleCommandSender && strings.length == 0) return true;

        FPlayer fPlayer = strings.length > 0 ? FPlayerManager.getPlayerFromName(strings[0]) : fCommand.getFPlayer();

        if(fPlayer == null){
            fCommand.sendMeMessage("reply.no_online");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        int currentPing = fPlayer.getPlayer().getPing();
        int badPing = Main.config.getInt("ping.bad.count");
        int mediumPing = Main.config.getInt("ping.medium.count");
        String pingColor;

        if(currentPing > badPing) pingColor = Main.config.getFormatString("ping.bad.color", commandSender);
        else if (currentPing > mediumPing) pingColor = Main.config.getFormatString("ping.medium.color", commandSender);
        else pingColor = Main.config.getFormatString("ping.good.color", commandSender);

        pingColor += currentPing;

        if(strings.length == 0 || commandSender == fCommand.getPlayer()){
            fCommand.sendMeMessage("ping.myself.message", "<ping>", pingColor);
            return true;
        }

        String[] replaceStrings = {"<player>", "<ping>"};
        String[] replaceTos = {fPlayer.getRealName(), pingColor};

        fCommand.sendMeMessage("ping.player.message", replaceStrings, replaceTos);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isOnlinePlayer(strings[0]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
