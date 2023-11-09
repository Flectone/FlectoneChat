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
import net.flectone.chat.model.metric.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

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

        if (FileManager.config.getBoolean("plugin.bStats.enable")) {
            Metrics bStats = new Metrics(this, 20209);
            bStats.addCustomChart(new Metrics.SimplePie("plugin_language", () -> FileManager.config.getString("plugin.language")));
            bStats.addCustomChart(new Metrics.AdvancedPie("modules", () -> {
                Map<String, Integer> map = new HashMap<>();
                FlectoneChat.getModuleManager().getModules().forEach(fModule -> map.put(fModule.getName(), 1));
                return map;
            }));
        }

        checkLastPluginVersion();
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
        database.getExecutor().close();
        database.disconnect();
    }

    public static void info(String message) {
        getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        getInstance().getLogger().warning(message);
    }

    private void checkLastPluginVersion() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/flectonechat/version"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(s -> {
                    JSONParser parser = new JSONParser();
                    try {
                        JSONArray json = (JSONArray) parser.parse(s);

                        String currentVersion = this.getDescription().getVersion();
                        String lastVersion = (String) ((JSONObject) json.get(0)).get("version_number");

                        if (FileManager.compareVersions(currentVersion, lastVersion) == -1) {
                            warning("Upgrade your " + currentVersion + " version of plugin to " + lastVersion);
                            warning("Url: https://modrinth.com/plugin/flectonechat/version/" + lastVersion);
                        }

                    } catch (ParseException e) {
                        warning("⚠ Failed to get latest plugin version");
                        e.printStackTrace();
                    }

                });
    }
}
