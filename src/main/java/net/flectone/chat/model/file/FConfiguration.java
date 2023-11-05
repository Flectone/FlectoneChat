package net.flectone.chat.model.file;

import net.flectone.chat.module.integrations.IntegrationsModule;
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
        ArrayList<String> playerGroups = IntegrationsModule.getGroups(player);
        if (playerGroups == null) playerGroups = new ArrayList<>(List.of("default"));
        else if (!playerGroups.contains("default")) playerGroups.add("default");

        ArrayList<String> arrayList = new ArrayList<>();
        for (String group : playerGroups) {
            ConfigurationSection configurationSection = this.getConfigurationSection(group + "." + string);
            if (configurationSection == null) continue;

            arrayList.addAll(configurationSection.getKeys(false));
        }

        return arrayList;
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
    public String getVaultString(@Nullable CommandSender sender, @NotNull String string) {
        List<String> playerGroups = null;

        if (sender instanceof Player player) {
            playerGroups = IntegrationsModule.getGroups(player);
        }

        if (playerGroups == null) playerGroups = List.of("default");

        String vaultString = "";

        for (String group : playerGroups) {
            String value = this.getString(group + "." + string);
            if (!value.isEmpty()) {
                vaultString = value;
                break;
            }
        }

        return vaultString.isEmpty()
                ? this.getString("default." + string)
                : vaultString;
    }

    public boolean getVaultBoolean(@Nullable CommandSender sender, @NotNull String string) {
        List<String> playerGroups = null;

        if (sender instanceof Player player) {
            playerGroups = IntegrationsModule.getGroups(player);
        }

        if (playerGroups == null) playerGroups = List.of("default");

        String vaultString = "";

        for (String group : playerGroups) {
            String value = this.getString(group + "." + string);
            if (!value.isEmpty()) {
                vaultString = value;
                break;
            }
        }

        return vaultString.isEmpty()
                ? this.getBoolean("default." + string)
                : Boolean.parseBoolean(vaultString);
    }

    public int getVaultInt(@NotNull CommandSender sender, @NotNull String string) {
        List<String> playerGroups = null;

        if (sender instanceof Player player) {
            playerGroups = IntegrationsModule.getGroups(player);
        }

        if (playerGroups == null) playerGroups = List.of("default");

        String vaultString = "";

        for (String group : playerGroups) {
            String value = this.getString(group + "." + string);
            if (!value.isEmpty()) {
                vaultString = value;
                break;
            }
        }

        return vaultString.isEmpty()
                ? this.getInt("default." + string)
                : Integer.parseInt(vaultString);
    }

    public List<String> getVaultStringList(@NotNull CommandSender sender, @NotNull String string) {
        List<String> playerGroups = null;

        if (sender instanceof Player player) {
            playerGroups = IntegrationsModule.getGroups(player);
        }

        if (playerGroups == null) playerGroups = List.of("default");

        ArrayList<String> arrayList = new ArrayList<>();

        for (String group : playerGroups) {
            String value = this.getString(group + "." + string);
            if (value.isEmpty()) continue;

            arrayList.addAll(this.getStringList(group + "." + string));
        }

        return arrayList.isEmpty()
                ? this.getStringList("default." + string)
                : arrayList;
    }
}