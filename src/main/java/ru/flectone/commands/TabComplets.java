package ru.flectone.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.flectone.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TabComplets implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        //create new list
        List<String> wordsList = new ArrayList<>();
        //command
        switch(command.getName().replace(" ", "")){
            case "chatcolor":{
                if(args.length == 1){
                    wordsList.add("default");
                    wordsList.add("#1abaf0");
                    wordsList.add("&b");
                }
                if(args.length == 2) {
                    wordsList.add("##77d7f7");
                    wordsList.add("&f");
                }
                break;
            }

            case "msg":
            case "ignore": {
                if(args.length == 1){
                    for(Player player : Bukkit.getOnlinePlayers()){
                        isStartsWith(args[0], player.getName(), wordsList);
                    }
                }
                if(args.length == 2) {
                    isStartsWith(args[1], "(message)", wordsList);
                }
                break;
            }

            case "reply":
            case "me":
            case "try": isStartsWith(args[0], "(message)", wordsList); break;
            case "try-cube":{
                if(args.length != 1) break;
                for(int x = 1; x <= Main.getInstance().getConfig().getInt("try-cube.max_amount"); x++){
                    isStartsWith(args[0], String.valueOf(x), wordsList);
                }
                break;
            }

            case "flectonechat":{
                if(args.length == 1){
                    isStartsWith(args[0], "reload", wordsList);
                    isStartsWith(args[0], "config", wordsList);
                    isStartsWith(args[0], "locale", wordsList);
                }
                if(args.length == 2){

                    if(args[0].equalsIgnoreCase("config")){
                        addKeysFile(Main.config.getKeys(), wordsList, args[1]);
                    }
                    if(args[0].equalsIgnoreCase("locale")){
                        addKeysFile(Main.locale.getKeys(), wordsList, args[1]);
                        sender.sendMessage(String.valueOf(Main.locale.getKeys().size()));
                    }

                }
                if(args.length == 3) {
                    isStartsWith(args[2], "set", wordsList);
                }
                if(args.length == 4){
                    isStartsWith(args[3], "string", wordsList);
                    isStartsWith(args[3], "integer", wordsList);
                    isStartsWith(args[3], "boolean", wordsList);
                }
            }
        }
        Collections.sort(wordsList);
        return wordsList;
    }

    private void isStartsWith(String arg, String string, List<String> wordsList){
        if(string.toLowerCase().startsWith(arg.toLowerCase()) || arg.replace(" ", "").isEmpty()){
            wordsList.add(string);
        }
    }

    //if keys starts with arg then add to words
    private void addKeysFile(Set<String> keys, List<String> wordsList, String arg){
        for(String key : keys){
            isStartsWith(arg, key, wordsList);
        }
    }
}
