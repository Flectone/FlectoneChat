package ru.flectone.commands;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.utils.ObjectUtils;
import ru.flectone.utils.Utils;

public class CommandStream implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(strings.length < 1 || !strings[0].equalsIgnoreCase("start") && !strings[0].equalsIgnoreCase("end")){
            fCommand.sendUsageMessage();
            return true;
        }

        if(strings.length == 1 && strings[0].equalsIgnoreCase("start")){
            fCommand.sendMeMessage("stream.need_url");
            return true;
        }

        if(!fCommand.isConsole()){
            if(!fCommand.getFPlayer().isStreamer() && strings[0].equalsIgnoreCase("end")){
                fCommand.sendMeMessage("stream.not");
                return true;
            }

            if(fCommand.getFPlayer().isStreamer() && strings[0].equalsIgnoreCase("start")){
                fCommand.sendMeMessage("stream.already");
                return true;
            }

            if(strings[0].equalsIgnoreCase("end")){
                fCommand.getFPlayer().setStreamer(false);
                fCommand.getFPlayer().removeFromName(Main.config.getFormatString("stream.prefix", commandSender));
                fCommand.sendMeMessage("stream.end");
                return true;
            }

            if(fCommand.isHaveCD()) return true;

            fCommand.getFPlayer().setStreamer(true);
            fCommand.getFPlayer().addToName(Main.config.getFormatString("stream.prefix", commandSender));
        }

        for(Player playerOnline : Bukkit.getOnlinePlayers()){

            ComponentBuilder streamAnnounce = new ComponentBuilder();

            for(String string : Main.locale.getStringList("stream.start")){

                string = string
                        .replace("<links>", ObjectUtils.toString(strings, 1))
                        .replace("<player>", fCommand.getSenderName());

                string = Utils.translateColor(string, playerOnline);

                Utils.buildMessage(string, streamAnnounce,  ChatColor.getLastColors(string), playerOnline);
                streamAnnounce.append("\n");
            }

            playerOnline.spigot().sendMessage(streamAnnounce.create());
        }

        return true;
    }
}
