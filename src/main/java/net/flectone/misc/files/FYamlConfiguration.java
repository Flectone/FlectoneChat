package net.flectone.misc.files;

import net.flectone.utils.ObjectUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class FYamlConfiguration extends YamlConfiguration {

    private final File file;
    private final String resourceFilePath;

    public FYamlConfiguration(@NotNull File file, @NotNull String resourceFilePath) {
        this.file = file;
        this.resourceFilePath = resourceFilePath;
        load();
    }

    @NotNull
    public String getResourceFilePath() {
        return resourceFilePath;
    }

    public void load() {
        try {
            super.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
    }

    public void save() {
        try {
            super.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @param path File string
     * @return String from yaml file
     */
    @NotNull
    @Override
    public String getString(@NotNull String path) {
        String string = super.getString(path);
        return string != null ? string : "";
    }

    /**
     * @param string File string
     * @param recipient Player who received
     * @param sender Player who sent
     * @return Formatted color string
     */
    public String getFormatString(String string, CommandSender recipient, CommandSender sender) {
        string = getString(string);
        return ObjectUtil.formatString(string, recipient, sender);
    }

    /**
     * @param string File string
     * @param sender Player who sent and received
     * @return Formatted color string
     */
    public String getFormatString(String string, CommandSender sender) {
        string = getString(string);
        return ObjectUtil.formatString(string, sender);
    }
}
