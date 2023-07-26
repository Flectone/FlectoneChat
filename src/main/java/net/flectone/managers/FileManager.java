package net.flectone.managers;

import net.flectone.Main;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class FileManager extends FileConfiguration {

    private FileConfiguration fileConfiguration;
    private File file;

    public FileManager(String path) {
        this.file = new File(Main.getInstance().getDataFolder() + File.separator + path);

        if (path.contains("language")) {
            checkExists("language/ru.yml");
            checkExists("language/en.yml");
        } else checkExists(path);

        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);

        InputStreamReader defConfigStream = new InputStreamReader(Main.getInstance().getResource(path), StandardCharsets.UTF_8);

        YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);

        internalLangConfig.getKeys(true).parallelStream()
                .filter(string -> !fileConfiguration.contains(string))
                .forEach(string -> fileConfiguration.set(string, internalLangConfig.get(string)));

        try {
            fileConfiguration.save(file);
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    private void checkExists(String path) {
        if (!new File(Main.getInstance().getDataFolder() + File.separator + path).exists())
            Main.getInstance().saveResource(path, false);
    }

    public void saveFile() {
        try {

            fileConfiguration.save(file);
            this.file = new File(file.getPath());

        } catch (IOException error) {
            Main.getInstance().getLogger().warning(error.getLocalizedMessage());
        }
    }

    public Set<String> getKeys() {
        return fileConfiguration.getKeys(true);
    }


    public List<String> getStringList(String path) {
        return fileConfiguration.getStringList(path);
    }


    public void set(String string, List<String> stringList) {
        fileConfiguration.set(string, stringList);
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    @Override
    public String getString(@NotNull String string) {
        return fileConfiguration.getString(string);
    }

    @Override
    public int getInt(@NotNull String string) {
        return Integer.parseInt(fileConfiguration.getString(string));
    }

    @Override
    public boolean getBoolean(@NotNull String string) {
        return Boolean.parseBoolean(fileConfiguration.getString(string));
    }

    public String getFormatString(String string, CommandSender sender, CommandSender papiPlayer) {
        string = fileConfiguration.getString(string);
        return ObjectUtil.formatString(string, sender, papiPlayer);
    }

    public String getFormatString(String string, CommandSender sender) {
        string = fileConfiguration.getString(string);
        return ObjectUtil.formatString(string, sender);
    }

    @Override
    public @NotNull String saveToString() {
        return "";
    }

    @Override
    public void loadFromString(@NotNull String contents) {

    }

    public void setObject(String path, Object object) {
        fileConfiguration.set(path, object);
    }
}
