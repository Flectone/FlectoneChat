package net.flectone;

import net.flectone.custom.FTabCompleter;
import net.flectone.integrations.expansions.FExpansion;
import net.flectone.integrations.luckperms.FLuckPerms;
import net.flectone.integrations.vault.FVault;
import net.flectone.integrations.voicechats.simplevoicechat.RegisterSimpleVoiceChat;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.TickerManager;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.tickers.PlayerPingTicker;
import net.flectone.utils.MetricsUtil;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.WebUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class Main extends JavaPlugin {

    public static boolean isHavePAPI = false;

    public static boolean isHavePlasmoVoice = false;

    public static boolean isHaveInteractiveChat = false;
    public static FileManager config;
    public static FileManager locale;
    private static Main instance;
    private Database database;

    public static Main getInstance() {
        return instance;
    }

    public static Database getDatabase() {
        return getInstance().database;
    }

    public static void info(String message) {
        getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        getInstance().getLogger().warning(message);
    }

    @Override
    public void onEnable() {
        new MetricsUtil(this, 16733);

        instance = this;

        config = new FileManager("config.yml");
        locale = new FileManager("language/" + config.getString("language") + ".yml");
        loadIcons();
        FPlayerManager.setScoreBoard();

        this.database = new SQLite(this);
        this.database.load();

        FPlayerManager.loadPlayers();
        FPlayerManager.loadBanList();

        registerClasses();
        hookPlugins();

        TickerManager.start();

        info("✔ Plugin enabled");

        WebUtil.checkNewerVersion();
    }

    private void registerClasses() {
        NMSUtil.registerClasses("net.flectone.listeners", (fClass) ->
                Bukkit.getServer().getPluginManager().registerEvents((Listener) fClass.getDeclaredConstructor().newInstance(), this));

        NMSUtil.registerClasses("net.flectone.commands", (fClass) -> {
            FTabCompleter fTabCompleter = (FTabCompleter) fClass.getDeclaredConstructor().newInstance();
            PluginCommand pluginCommand = Main.getInstance().getCommand(fTabCompleter.getCommandName());

            if (pluginCommand == null) return;

            pluginCommand.setExecutor(fTabCompleter);
            pluginCommand.setTabCompleter(fTabCompleter);
        });
    }

    private void loadIcons() {
        String path = Main.getInstance().getDataFolder() + File.separator + "icons" + File.separator;

        List<String> iconNames = Main.config.getStringList("server.icon.names");
        iconNames.add("maintenance");

        for (String iconName : iconNames) {
            if (new File(path + iconName + ".png").exists()) continue;

            Main.getInstance().saveResource("icons" + File.separator + iconName + ".png", false);
        }
    }

    private void hookPlugins() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            FVault.register();
            getLogger().info("\uD83D\uDD12 Vault detected and hooked");
        }

        if (Bukkit.getPluginManager().getPlugin("voicechat") != null) {
            new RegisterSimpleVoiceChat();
            getLogger().info("\uD83D\uDD12 SimpleVoiceChat detected and hooked");
        }

        if (Bukkit.getPluginManager().getPlugin("plasmovoice") != null) {
            isHavePlasmoVoice = true;
            getLogger().info("\uD83D\uDD12 PlasmoVoice detected and hooked");
        }

        if (Bukkit.getPluginManager().getPlugin("InteractiveChat") != null) {
            isHaveInteractiveChat = true;
            getLogger().info("\uD83D\uDD12 InteractiveChat detected and hooked");
        }

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            new FLuckPerms(this);
            getLogger().info("\uD83D\uDD12 LuckPerms detected and hooked");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isHavePAPI = true;
            getLogger().info("\uD83D\uDD12 PlaceholderAPI detected and hooked");
            new FExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        FPlayerManager.uploadPlayers();
        FPlayerManager.removePlayersFromTeams();
        PlayerPingTicker.unregisterPingObjective();
        info("✔ Plugin disabled");
    }
}
