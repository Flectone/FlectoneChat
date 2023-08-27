package net.flectone;

import net.flectone.listeners.PlayerAdvancementDoneListener;
import net.flectone.listeners.PlayerDeathEventListener;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.HookManager;
import net.flectone.managers.TickerManager;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.testing.ServerBrand;
import net.flectone.tickers.PlayerPingTicker;
import net.flectone.utils.MetricsUtil;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.WebUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

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

        FileManager.initialize();
        FPlayerManager.setScoreBoard();

        this.database = new SQLite(this);
        this.database.load();

        FPlayerManager.loadPlayers();
        FPlayerManager.loadBanList();

        registerClasses();
        HookManager.hookPlugins();

        TickerManager.start();
        PlayerDeathEventListener.reload();
        PlayerAdvancementDoneListener.reload();

        if (FileManager.config.getBoolean("server.brand.enable")) new ServerBrand();

        info("✔ Plugin enabled");

        Bukkit.getScheduler().runTaskAsynchronously(this, WebUtil::checkNewerVersion);
    }

    private void registerClasses() {
        NMSUtil.registerClasses("net.flectone.listeners", (fClass) ->
                Bukkit.getServer().getPluginManager().registerEvents((Listener) fClass.getDeclaredConstructor().newInstance(), this));

        NMSUtil.registerClasses("net.flectone.commands", (fClass) -> {
            FTabCompleter fTabCompleter = (FTabCompleter) fClass.getDeclaredConstructor().newInstance();
            PluginCommand pluginCommand = Main.getInstance().getCommand(fTabCompleter.getCommandName());

            if (pluginCommand == null) return;
            if (!fTabCompleter.isEnable()) return;

            pluginCommand.setExecutor(fTabCompleter);
            pluginCommand.setTabCompleter(fTabCompleter);
        });
    }

    @Override
    public void onDisable() {
        PlayerPingTicker.unregisterPingObjective();
        FPlayerManager.clearPlayers();
        info("✔ Plugin disabled");
    }
}
