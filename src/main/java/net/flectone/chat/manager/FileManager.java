package net.flectone.chat.manager;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FileManager {

    private static final String DATA_FOLDER = FlectoneChat.getPlugin().getDataFolder().getAbsolutePath() + File.separator;
    private static final String SETTINGS_FOLDER = "settings" + File.separator;
    private static final String LANGUAGES_FOLDER = SETTINGS_FOLDER + "languages" + File.separator;
    private static final String ICONS_FOLDER = SETTINGS_FOLDER + "icons" + File.separator;

    private final HashMap<String, File> iconsMap = new HashMap<>();

    @Getter
    private FConfiguration config;
    @Getter
    private FConfiguration locale;
    @Getter
    private FConfiguration modules;
    @Getter
    private FConfiguration commands;
    @Getter
    private FConfiguration sounds;
    @Getter
    private FConfiguration integrations;
    @Getter
    private FConfiguration swears;
    @Getter
    private FConfiguration cooldowns;

    public void init() {
        config = Type.CONFIG.load();

        Type.LOCALE_EN.load();
        Type.LOCALE_RU.load();

        Type localeType = Type.fromString(config.getString("plugin.language"));
        locale = localeType != null ? localeType.getFile() : Type.LOCALE_EN.getFile();

        modules = Type.MODULES.load();
        commands = Type.COMMANDS.load();
        sounds = Type.SOUNDS.load();
        integrations = Type.INTEGRATIONS.load();
        cooldowns = Type.COOLDOWNS.load();

        if (modules.getBoolean("player-message.swear-protection.enable")) {
            swears = Type.SWEARS.load();
        }

        loadIcons();

        checkMigration();
    }

    public void checkMigration() {
        String fileVersion = config.getString("plugin.version");
        String projectVersion = FlectoneChat.getPlugin().getDescription().getVersion();

        if (compareVersions(fileVersion, projectVersion) == -1) {
            Arrays.stream(Type.values()).forEach(Type::update);
        } else return;

        config.set("plugin.version", projectVersion);
        config.save();

        FlectoneChat.warning("Your configs have been updated to " + projectVersion + " version");
    }

    public static FConfiguration loadFile(String filePath) {
        File file = new File(DATA_FOLDER + filePath);
        FConfiguration fileConfiguration = null;

        try {
            if (!file.exists()) {
                FlectoneChat.getPlugin().saveResource(filePath, false);
            }

            fileConfiguration = new FConfiguration(file, filePath);

        } catch (IllegalArgumentException exception) {
            FlectoneChat.warning("Failed to save " + filePath + " file");
            exception.printStackTrace();
        }

        return fileConfiguration;
    }

    public File getIcon(String icon) {
        if (iconsMap.get(icon) != null) return iconsMap.get(icon);

        File fileIcon = new File(DATA_FOLDER + ICONS_FOLDER + icon + ".png");
        iconsMap.put(icon, fileIcon);

        return fileIcon;
    }

    private void loadIcons() {
        List<String> iconNames = config.getStringList("default.server.status.icon.names");
        iconNames.add("maintenance");

        iconNames.stream()
                .filter(icon -> !getIcon(icon).exists()
                        && FlectoneChat.getPlugin().getResource(ICONS_FOLDER + icon + ".png") != null)
                .forEach(icon ->
                        FlectoneChat.getPlugin().saveResource(ICONS_FOLDER + icon + ".png", false));

    }

    public int compareVersions(@NotNull String firstVersion, @NotNull String secondVersion) {
        if (firstVersion.isEmpty()) return -1;
        if (secondVersion.isEmpty()) return 1;

        String[] parts1 = firstVersion.split("\\.");
        String[] parts2 = secondVersion.split("\\.");

        for (int x = 0; x < parts1.length; x++) {
            int num1 = Integer.parseInt(parts1[x]);
            int num2 = Integer.parseInt(parts2[x]);

            if (num1 > num2) return 1;
            else if (num1 < num2) return -1;
        }

        return 0;
    }

    public enum Type {

        CONFIG("", "config"),
        LOCALE_EN(LANGUAGES_FOLDER, "en"),
        LOCALE_RU(LANGUAGES_FOLDER, "ru"),
        MODULES(SETTINGS_FOLDER, "modules"),
        COMMANDS(SETTINGS_FOLDER, "commands"),
        SOUNDS(SETTINGS_FOLDER, "sounds"),
        INTEGRATIONS(SETTINGS_FOLDER, "integrations"),
        SWEARS(SETTINGS_FOLDER, "swears", false),
        COOLDOWNS(SETTINGS_FOLDER, "cooldowns");

        @Getter
        private FConfiguration file;
        private final String filePath;

        @Getter
        private final String fileName;
        Type(@NotNull String filePath, @NotNull String fileName) {
            this.filePath = filePath;
            this.fileName = fileName;
            load();
        }

        Type(@NotNull String filePath, @NotNull String fileName, boolean isEnabled) {
            this.filePath = filePath;
            this.fileName = fileName;
            if (!isEnabled) return;
            load();
        }

        public FConfiguration load() {
            this.file = loadFile(filePath + fileName + ".yml");
            return file;
        }

        public void update() {
            if (this.file == null) return;
            InputStream inputStream = FlectoneChat.getPlugin().getResource(file.getResourceFilePath().replace('\\', '/'));

            if (inputStream == null) return;

            InputStreamReader defConfigStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            YamlConfiguration resourceFile = YamlConfiguration.loadConfiguration(defConfigStream);

            resourceFile.getKeys(true).parallelStream()
                    .filter(string -> {
                        if (!file.contains(string)) return true;

                        Object objectA = file.get(string);
                        Object objectB = resourceFile.get(string);

                        return objectA != null && objectB != null && !objectA.getClass().equals(objectB.getClass());
                    })
                    .forEach(string -> file.set(string, resourceFile.get(string)));

            file.save();
        }

        @Nullable
        public static Type fromString(@NotNull String string) {
            return Arrays.stream(Type.values())
                    .filter(type -> type.getFileName().equalsIgnoreCase(string))
                    .findAny()
                    .orElse(null);
        }
    }
}
