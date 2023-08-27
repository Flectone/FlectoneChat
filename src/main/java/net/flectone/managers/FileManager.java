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
        config = load("config.yml");

        String version = Main.getInstance().getDescription().getVersion();

        if (!version.equals(config.getString("version"))) {
            Main.warning("⚠ Your configs have been updated to " + version);

            config.set("version", version);
            migrate(config);
            loadLocale(true);
        } else loadLocale(false);

        loadIcons();
    }

    private static void loadLocale(boolean needMigrate) {
        FYamlConfiguration ruLocale = load(languagesPath + "ru.yml");
        FYamlConfiguration enLocale = load(languagesPath + "en.yml");

        if (needMigrate) {
            migrate(ruLocale);
            migrate(enLocale);
        }

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

    public static FYamlConfiguration load(String filePath) {
        File file = new File(dataFolder + filePath);

        if(!file.exists()) Main.getInstance().saveResource(filePath, false);

        FYamlConfiguration fileConfiguration = new FYamlConfiguration(file, filePath);

        try {
            fileConfiguration.save(file);
        } catch (IOException exception) {
            Main.warning("Failed to save " + filePath + " file");
            exception.printStackTrace();
        }

        return fileConfiguration;
    }

    private static void migrate(FYamlConfiguration fileConfiguration) {
        InputStream inputStream = Main.getInstance().getResource(fileConfiguration.getResourceFilePath());

        if (inputStream == null) return;

        InputStreamReader defConfigStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);

        internalLangConfig.getKeys(true).parallelStream()
                .filter(string -> !fileConfiguration.contains(string)
                        || !fileConfiguration.get(string).getClass().equals(internalLangConfig.get(string).getClass()))
                .forEach(string -> fileConfiguration.set(string, internalLangConfig.get(string)));
    }
}
