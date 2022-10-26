package ru.flectone.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.flectone.FPlayer;
import ru.flectone.Main;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileResource extends FileConfiguration {

    private FileConfiguration fileConfiguration;
    private File file;

    public FileResource(String path){
        this.file = new File(Main.getInstance().getDataFolder() + File.separator + path);

        if(path.contains("language")){
            checkExists("language/ru.yml");
            checkExists("language/en.yml");
        } else checkExists(path);

        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    private void checkExists(String path){
        if(!new File(Main.getInstance().getDataFolder() + File.separator + path).exists())
            Main.getInstance().saveResource(path, false);
    }

    public void saveFile(){
        try {

            fileConfiguration.save(file);
            this.file = new File(file.getPath());

        } catch (IOException error){
            Main.getInstance().getLogger().warning(error.getLocalizedMessage());
        }
    }

    public Set<String> getKeys(){
        return fileConfiguration.getKeys(true);
    }


    public List<String> getStringList(String path){
        return fileConfiguration.getStringList(path);
    }


    public void set(String string, List<String> stringList){
        fileConfiguration.set(string, stringList);
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    @Override
    public String getString(String string){
        return fileConfiguration.getString(string);
    }

    @Override
    public int getInt(String string){
        return Integer.parseInt(fileConfiguration.getString(string));
    }

    @Override
    public boolean getBoolean(String string){
        return Boolean.parseBoolean(fileConfiguration.getString(string));
    }

    public String getFormatString(String string, Player player){

        string = fileConfiguration.getString(string);

        if(player != null){
            FPlayer fPlayer = PlayerUtils.getPlayer(player);

            string = string
                    .replace("&&1", fPlayer.getColors().get(0))
                    .replace("&&2", fPlayer.getColors().get(1));
        } else {
            string = string
                    .replace("&&1", Main.getInstance().config.getString("color.first"))
                    .replace("&&2", Main.getInstance().config.getString("color.second"));
        }

        return Utils.translateColor(string);
    }

    @Override
    public String saveToString() {
        return null;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {

    }

    public void setObject(String path, Object object) {
        fileConfiguration.set(path, object);
    }
}
