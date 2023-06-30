package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.flectone.Main;
import ru.flectone.custom.FCommands;
import ru.flectone.custom.FPlayer;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.ObjectUtils;
import ru.flectone.utils.PlayerUtils;

public class CommandFlectonechat implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(strings.length < 1 || !strings[0].equals("reload") && strings.length < 5){
            fCommand.sendUsageMessage();
            return true;
        }

        if(!strings[0].equals("reload")){

            if(!strings[2].equals("set") || !strings[3].equals("boolean") && !strings[3].equals("integer") && !strings[3].equals("string")){
                fCommand.sendUsageMessage();
                return true;
            }


            if(!Main.config.getKeys().contains(strings[1]) && !Main.locale.getKeys().contains(strings[1])){
                fCommand.sendMeMessage( "flectonechat.not_exist");
                return true;
            }

            Object object;
            if (strings.length > 5) {
                object = ObjectUtils.toString(strings, 4);
            } else {
                object = getObject(strings[3], strings[4]);
            }

            //set and save file .yml
            switch(strings[0]){
                case "config":
                    Main.config.setObject(strings[1], object);
                    Main.config.saveFile();

                    Main.locale.setFileConfiguration(new FileResource("language/" + Main.config.getString("language") + ".yml"));
                    break;
                case "locale":
                    Main.locale.setObject(strings[1], object);
                    Main.locale.saveFile();
                    break;
            }
        }

        Main.getInstance().reloadConfig();

        for(Player playerOnline : Bukkit.getOnlinePlayers()){
            PlayerUtils.removePlayer(playerOnline);
            new FPlayer(playerOnline);
        }

        fCommand.sendMeMessage("flectonechat.message");

        return true;
    }

    private Object getObject(String objectName, String arg){
        switch(objectName.toLowerCase()){
            case "string": return arg;
            case "boolean": return Boolean.parseBoolean(arg);
            default: return Integer.valueOf(arg);
        }
    }
}
