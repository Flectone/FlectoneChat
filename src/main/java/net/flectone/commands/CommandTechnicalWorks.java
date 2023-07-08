package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandTechnicalWorks extends FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isInsufficientArgs(1)) return true;

        if(!strings[0].equalsIgnoreCase("on") && !strings[0].equalsIgnoreCase("off")){
            fCommand.sendUsageMessage();
            return true;
        }

        boolean isTechnicalWorks = Main.config.getBoolean("command.technical-works.enable");

        if(isTechnicalWorks && strings[0].equalsIgnoreCase("on")){
            fCommand.sendMeMessage("command.technical-works.turned-on.already");
            return true;
        }

        if(!isTechnicalWorks && strings[0].equalsIgnoreCase("off")){
            fCommand.sendMeMessage("command.technical-works.turned-off.not");
            return true;
        }

        if(fCommand.isHaveCD()) return true;

        isTechnicalWorks = strings[0].equalsIgnoreCase("on");

        if(isTechnicalWorks){
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> !player.isOp() && !player.hasPermission(Main.config.getString("command.technical-works.permission")))
                    .forEach(player -> player.kickPlayer(Main.locale.getFormatString("command.technical-works.kicked-message", null)));
        }

        fCommand.sendMeMessage("command.technical-works.turned-" + strings[0].toLowerCase() + ".message");
        Main.config.setObject("command.technical-works.enable", isTechnicalWorks);
        Main.config.saveFile();

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "on");
            isStartsWith(strings[0], "off");
        }

        return wordsList;
    }
}
