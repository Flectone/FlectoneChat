package net.flectone.chat.manager;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class FileManager {

    private static final String DATA_FOLDER = FlectoneChat.getInstance().getDataFolder().getAbsolutePath() + File.separator;
    private static final String SETTINGS_FOLDER = "settings" + File.separator;
    private static final String LANGUAGES_FOLDER = SETTINGS_FOLDER + "languages" + File.separator;
    private static final String ICONS_FOLDER = SETTINGS_FOLDER + "icons" + File.separator;

    private static final HashMap<String, File> ICONS_MAP = new HashMap<>();

    public static FConfiguration config;
    public static FConfiguration locale;
    public static FConfiguration modules;
    public static FConfiguration commands;
    public static FConfiguration sounds;
    public static FConfiguration integrations;
    public static FConfiguration swears;
    public static FConfiguration cooldowns;

    public static void init() {
        config = Type.CONFIG.load();
        locale = Type.LOCALE.load();
        modules = Type.MODULES.load();
        commands = Type.COMMANDS.load();
        sounds = Type.SOUNDS.load();
        integrations = Type.INTEGRATIONS.load();
        cooldowns = Type.COOLDOWNS.load();

        if (modules.getBoolean("player-message.swear-protection.enable")) {
            swears = Type.SWEARS.load();
        }

        loadIcons();
    }

    public static FConfiguration loadFile(String filePath) {
        File file = new File(DATA_FOLDER + filePath);
        FConfiguration fileConfiguration = null;

        try {
            if (!file.exists()) {
                FlectoneChat.getInstance().saveResource(filePath, false);
            }

            fileConfiguration = new FConfiguration(file, filePath);

        } catch (IllegalArgumentException exception) {
            FlectoneChat.warning("Failed to save " + filePath + " file");
            exception.printStackTrace();
        }

        return fileConfiguration;
    }

    public static File getIcon(String icon) {
        if (ICONS_MAP.get(icon) != null) return ICONS_MAP.get(icon);

        File fileIcon = new File(DATA_FOLDER + ICONS_FOLDER + icon + ".png");
        ICONS_MAP.put(icon, fileIcon);

        return fileIcon;
    }

    private static void loadIcons() {
        List<String> iconNames = config.getStringList("default.server.status.icon.names");
        iconNames.add("maintenance");

        iconNames.stream()
                .filter(icon -> !getIcon(icon).exists()
                        && FlectoneChat.getInstance().getResource(ICONS_FOLDER + icon + ".png") != null)
                .forEach(icon ->
                        FlectoneChat.getInstance().saveResource(ICONS_FOLDER + icon + ".png", false));

    }

    public enum Type {

        CONFIG("", "config"),
        LOCALE(LANGUAGES_FOLDER, "en"),
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
    }
}
