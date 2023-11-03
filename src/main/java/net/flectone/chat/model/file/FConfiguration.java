package net.flectone.chat.model.file;

import net.flectone.chat.util.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FConfiguration extends YamlConfiguration {

    private final File file;
    private final String resourceFilePath;

    public FConfiguration(@NotNull File file, @NotNull String resourceFilePath) {
        this.file = file;
        this.resourceFilePath = resourceFilePath;
        load();
    }


    public void save() {
        try {
            super.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @NotNull
    public List<String> getCustomList(@Nullable Player player, @NotNull String string) {
        String playerGroup = PlayerUtil.getVaultGroup(player);

        ConfigurationSection configurationSection = this.getConfigurationSection(playerGroup + string);
        if (configurationSection == null) {
            configurationSection = this.getConfigurationSection("default." + string);
            if (configurationSection == null) return new ArrayList<>();
        }

        return new ArrayList<>(configurationSection.getKeys(false));
    }

    @NotNull
    @Override
    public String getString(@NotNull String string) {
        string = super.getString(string);

        return string != null
                ? string.replace("\\n", System.lineSeparator())
                : "";
    }

    @NotNull
    public String getVaultStringEmpty(@Nullable CommandSender sender, @NotNull String string) {
        return this.getString(PlayerUtil.getVaultGroup(sender) + "." + string);
    }

    @NotNull
    public String getVaultString(@Nullable CommandSender sender, @NotNull String string) {
        String vaultString = getVaultStringEmpty(sender, string);

        return vaultString.isEmpty()
                ? this.getString("default." + string)
                : vaultString;
    }

    public boolean getVaultBoolean(@Nullable CommandSender sender, @NotNull String string) {
        return  getVaultStringEmpty(sender, string).isEmpty()
                ? this.getBoolean("default." + string)
                : this.getBoolean(PlayerUtil.getVaultGroup(sender) + "." + string);
    }

    public int getVaultInt(@NotNull CommandSender sender, @NotNull String string) {
        return getVaultStringEmpty(sender, string).isEmpty()
                ? this.getInt("default." + string)
                : this.getInt(PlayerUtil.getVaultGroup(sender) + "." + string);
    }

    public List<String> getVaultStringList(@NotNull CommandSender sender, @NotNull String string) {
        return getVaultStringEmpty(sender, string).isEmpty()
                ? this.getStringList("default." + string)
                : this.getStringList(PlayerUtil.getVaultGroup(sender) + "." + string);
    }
}