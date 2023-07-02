package net.flectone.commands;

import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FileManager;
import net.flectone.managers.PlayerManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandFlectonechat extends FTabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(strings.length < 1 || !strings[0].equals("reload") && strings.length < 5){
            fCommand.sendUsageMessage();
            return true;
        }

        if(fCommand.isHaveCD()) return true;

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
                object = ObjectUtil.toString(strings, 4);
            } else {
                object = getObject(strings[3], strings[4]);
            }

            //set and save file .yml
            switch(strings[0]){
                case "config":
                    Main.config.updateFile(strings[1], object);
                    Main.locale.setFileConfiguration(new FileManager("language/" + Main.config.getString("language") + ".yml"));
                    break;
                case "locale":
                    Main.locale.updateFile(strings[1], object);
                    break;
            }
        }

        Main.getInstance().reloadConfig();
        Main.getInstance().startTabScheduler();
        Main.getInstance().checkPlayerMoveTimer();

        for(Player playerOnline : Bukkit.getOnlinePlayers()){
            PlayerManager.removePlayer(playerOnline);
            new FPlayer(playerOnline);
        }

        fCommand.sendMeMessage("flectonechat.message");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if(strings.length == 1){
            isStartsWith(strings[0], "reload");
            isStartsWith(strings[0], "config");
            isStartsWith(strings[0], "locale");
        } else if(strings.length == 2){

            if(strings[0].equalsIgnoreCase("config")){
                addKeysFile(Main.config, strings[1]);
            }
            if(strings[0].equalsIgnoreCase("locale")){
                addKeysFile(Main.locale, strings[1]);
            }

        } else if(strings.length == 3) {
            isStartsWith(strings[2], "set");
        } else if(strings.length == 4){
            isStartsWith(strings[3], "string");
            isStartsWith(strings[3], "integer");
            isStartsWith(strings[3], "boolean");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    private Object getObject(String objectName, String arg){
        switch(objectName.toLowerCase()){
            case "string": return arg;
            case "boolean": return Boolean.parseBoolean(arg);
            default: return Integer.valueOf(arg);
        }
    }
}
