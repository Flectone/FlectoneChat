package net.flectone.chat;

import lombok.Getter;
import lombok.Setter;
import net.flectone.chat.database.sqlite.Database;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.manager.FileManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.serverMessage.advancement.AdvancementModule;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public final class FlectoneChat extends JavaPlugin {

    @Getter
    private static FlectoneChat instance;

    @Getter
    @Setter
    private static FModuleManager moduleManager;

    @Getter
    @Setter
    private static Database database;

    @Getter
    private static Scoreboard scoreBoard;

    @Override
    public void onEnable() {
        instance = this;

        FileManager.init();

        setScoreBoard();

        database = new Database(this);

        moduleManager = new FModuleManager();
        moduleManager.init();

        loadOfflinePlayersToDatabase();
        FPlayerManager.loadOfflinePlayers();
        FPlayerManager.loadOnlinePlayers();
        FPlayerManager.loadTabCompleteData();
    }

    public static void setScoreBoard() {
        scoreBoard = FileManager.config.getBoolean("plugin.scoreboard.custom")
                ? Bukkit.getScoreboardManager().getNewScoreboard()
                : Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public static void loadOfflinePlayersToDatabase() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            database.offlineToDatabase(offlinePlayer);
        }
    }

    @Override
    public void onDisable() {

        FModule fModule = FlectoneChat.getModuleManager().get(AdvancementModule.class);
        if (fModule instanceof AdvancementModule advancementModule) {
            advancementModule.terminateAnnounce();
        }

        FPlayerManager.terminateAll();

        FActionManager.clearAll();
        FModuleManager.clear();
        database.getExecutor().close();
        database.disconnect();
    }

    public static void info(String message) {
        getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        getInstance().getLogger().warning(message);
    }
}
