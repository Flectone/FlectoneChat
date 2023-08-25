package net.flectone.managers;

import net.flectone.Main;
import net.flectone.misc.files.FYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileManager {

    private static final String dataFolder = Main.getInstance().getDataFolder().getAbsolutePath() + File.separator;
    private static final String languagesPath = "language" + File.separator;
    private static final String iconsPath = "icons" + File.separator;
    public static FYamlConfiguration config;
    public static FYamlConfiguration locale;

    public static void initialize() {
        config = load("config.yml", true);

        loadLocale();
        loadIcons();
    }

    private static void loadLocale() {
        FYamlConfiguration ruLocale = load(languagesPath + "ru.yml", true);
        FYamlConfiguration enLocale = load(languagesPath + "en.yml", true);

        locale = switch (config.getString("language")) {
            case "ru" -> ruLocale;
            default -> enLocale;
        };
    }

    private static void loadIcons() {
        List<String> iconNames = config.getStringList("server.icon.names");
        iconNames.add("maintenance");

        iconNames.stream().filter(icon -> !new File(dataFolder + iconsPath + icon + ".png").exists()
                        && Main.getInstance().getResource(iconsPath + icon + ".png") != null)
                .forEach(icon -> Main.getInstance().saveResource(iconsPath + icon + ".png", false));
    }

    public static FYamlConfiguration load(String filePath, boolean needMigrate) {
        File file = new File(dataFolder + filePath);

        if(!file.exists()) Main.getInstance().saveResource(filePath, false);

        FYamlConfiguration fileConfiguration = new FYamlConfiguration(file);

        if (needMigrate) migrate(fileConfiguration, filePath);

        try {
            fileConfiguration.save(file);
        } catch (IOException exception) {
            Main.warning("Failed to save " + filePath + " file");
            exception.printStackTrace();
        }

        return fileConfiguration;
    }

    private static void migrate(FYamlConfiguration fileConfiguration, String filePath) {
        InputStream inputStream = Main.getInstance().getResource(filePath);

        if (inputStream == null) return;

        InputStreamReader defConfigStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);

        internalLangConfig.getKeys(true).parallelStream()
                .filter(string -> !fileConfiguration.contains(string))
                .forEach(string -> fileConfiguration.set(string, internalLangConfig.get(string)));
    }
}
