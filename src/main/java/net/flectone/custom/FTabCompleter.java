package net.flectone.custom;

import net.flectone.managers.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTabCompleter implements CommandExecutor, TabCompleter {

    protected List<String> wordsList = new ArrayList<>();

    protected void isStartsWith(String arg, String string){
        if(string.toLowerCase().startsWith(arg.toLowerCase()) || arg.replace(" ", "").isEmpty()){
            wordsList.add(string);
        }
    }

    protected void addKeysFile(FileManager fileManager, String arg){
        for(String key : fileManager.getKeys()){

            if(fileManager.getString(key).contains("root='YamlConfiguration'")) continue;

            isStartsWith(arg, key);
        }
    }

    protected void isOfflinePlayer(String arg){
        Arrays.stream(Bukkit.getOfflinePlayers()).forEach(offlinePlayer -> {
            isStartsWith(arg, offlinePlayer.getName());
        });
    }

    protected void isOnlinePlayer(String arg){
        Bukkit.getOnlinePlayers().forEach(player -> {
            isStartsWith(arg, player.getName());
        });
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
