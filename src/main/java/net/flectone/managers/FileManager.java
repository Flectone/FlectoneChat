package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.files.FYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileManager {

    private static String lastVersion = "";
    private static final String dataFolder = Main.getInstance().getDataFolder().getAbsolutePath() + File.separator;
    private static final String languagesPath = "language" + File.separator;
    private static final String iconsPath = "icons" + File.separator;
    public static FYamlConfiguration config;
    public static FYamlConfiguration locale;

    public static void initialize() {
        config = load("config.yml");
        lastVersion = config.getString("version");

        String currentVersion = Main.getInstance().getDescription().getVersion();
        boolean needMigrate = false;

        if (compareVersions(currentVersion, lastVersion) == 1) {
            Main.warning("⚠ Your configs have been updated to " + currentVersion);

            config.set("version", currentVersion);
            config.save();

            migrate(config);
            needMigrate = true;
        }

        loadLocale(needMigrate);
        loadIcons();
    }

    private static void loadLocale(boolean needMigrate) {
        FYamlConfiguration ruLocale = load(languagesPath + "ru.yml");
        FYamlConfiguration enLocale = load(languagesPath + "en.yml");

        String customLocaleName = config.getString("language");
        FYamlConfiguration customLocale = switch (customLocaleName) {
            case "ru" -> ruLocale;
            case "en" -> enLocale;
            default -> load(languagesPath + customLocaleName + ".yml");
        };

        if (customLocale == null) {
            customLocale = enLocale;
        }

        if (needMigrate) {
            migrate(ruLocale);
            migrate(enLocale);
            migrate(customLocale);
        }

        locale = customLocale;
    }

    private static void loadIcons() {
        List<String> iconNames = config.getStringList("server.icon.names");
        iconNames.add("maintenance");

        iconNames.stream().filter(icon -> !new File(dataFolder + iconsPath + icon + ".png").exists()
                        && Main.getInstance().getResource(iconsPath + icon + ".png") != null)
                .forEach(icon -> Main.getInstance().saveResource(iconsPath + icon + ".png", false));
    }

    public static FYamlConfiguration load(String filePath) {
        File file = new File(dataFolder + filePath);
        FYamlConfiguration fileConfiguration = null;

        try {
            if (!file.exists()) Main.getInstance().saveResource(filePath, false);

            fileConfiguration = new FYamlConfiguration(file, filePath);
            fileConfiguration.save(file);

        } catch (IOException | IllegalArgumentException exception) {
            Main.warning("Failed to save " + filePath + " file");
            exception.printStackTrace();
        }

        return fileConfiguration;
    }

    private static void migrate(FYamlConfiguration oldFile) {
        InputStream inputStream = Main.getInstance().getResource(oldFile.getResourceFilePath().replace('\\', '/'));

        if (inputStream == null) return;

        InputStreamReader defConfigStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        YamlConfiguration resourceFile = YamlConfiguration.loadConfiguration(defConfigStream);

        resourceFile.getKeys(true).parallelStream()
                .filter(string -> {
                    if (!oldFile.contains(string)) return true;

                    Object objectA = oldFile.get(string);
                    Object objectB = resourceFile.get(string);

                    return objectA != null && objectB != null && !objectA.getClass().equals(objectB.getClass());
                })
                .forEach(string -> oldFile.set(string, resourceFile.get(string)));

        oldFile.save();
    }

    public static String getLastVersion() {
        return lastVersion;
    }

    /**
     * Compares two version strings and returns:<br>
     * -1 if version1 is less than version2,<br>
     * 0 if version1 is equal to version2, or<br>
     * 1 if version1 is greater than version2.<br>
     *
     * @param version1 The first version string
     * @param version2 The second version string
     * @return -1, 0, or 1 based on the comparison result
     */
    public static int compareVersions(@NotNull String version1, @NotNull String version2) {
        if (version1.isEmpty()) return -1;
        if (version2.isEmpty()) return 1;

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        for (int x = 0; x < parts1.length; x++) {
            int num1 = Integer.parseInt(parts1[x]);
            int num2 = Integer.parseInt(parts2[x]);


            if (num1 > num2) return 1;
            else if (num1 < num2) return -1;
        }

        return 0;
    }
}
