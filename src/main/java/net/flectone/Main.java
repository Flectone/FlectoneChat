package net.flectone;

import net.flectone.listeners.PlayerAdvancementDoneListener;
import net.flectone.listeners.PlayerDeathEventListener;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.HookManager;
import net.flectone.managers.TickerManager;
import net.flectone.misc.brand.ServerBrand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.sqlite.CustomThreadPool;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.tickers.PlayerPingTicker;
import net.flectone.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;

import static net.flectone.managers.FileManager.config;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private static CustomThreadPool dataThreadPool;
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

    public static CustomThreadPool getDataThreadPool() {
        return dataThreadPool;
    }

    @Override
    public void onEnable() {
        new MetricsUtil(this, 16733);

        dataThreadPool = new CustomThreadPool(1);

        instance = this;

        FileManager.initialize();
        FPlayerManager.setScoreBoard();

        dataThreadPool.execute(() -> {
            this.database = new SQLite(this);
            this.database.load();
        });

        FPlayerManager.loadPlayers();
        FPlayerManager.loadBanList();

        registerClasses();
        HookManager.hookPlugins();

        if (FileManager.config.getBoolean("server.brand.enable")) new ServerBrand();

        TickerManager.start();
        PlayerDeathEventListener.reload();
        PlayerAdvancementDoneListener.reload();

        if (config.getBoolean("chat.swear-protection.enable")) {
            BlackListUtil.loadSwears();
        }

        info("✔ Plugin enabled");

        Bukkit.getScheduler().runTaskAsynchronously(this, WebUtil::checkNewerVersion);
    }

    private void registerClasses() {
        NMSUtil.registerClasses("net.flectone.listeners", (fClass) ->
                Bukkit.getServer().getPluginManager().registerEvents((Listener) fClass.getDeclaredConstructor().newInstance(), Main.getInstance()));

        NMSUtil.registerClasses("net.flectone.commands", (fClass) -> {
            FTabCompleter fTabCompleter = (FTabCompleter) fClass.getDeclaredConstructor().newInstance();
            PluginCommand pluginCommand = Main.getInstance().getCommand(fTabCompleter.getCommandName());

            if (pluginCommand == null) return;

            CommandsUtil.unregisterCommand(pluginCommand);

            if (!fTabCompleter.isEnable()) return;

            List<String> aliases = config.getStringList("command." + pluginCommand.getName() + ".aliases");
            pluginCommand = CommandsUtil.createCommand(this, pluginCommand.getName(), aliases);
            CommandsUtil.getCommandMap().register("flectonechat", pluginCommand);

            pluginCommand.setExecutor(fTabCompleter);
            pluginCommand.setTabCompleter(fTabCompleter);
        });
    }

    @Override
    public void onDisable() {
        PlayerPingTicker.unregisterPingObjective();
        FPlayerManager.clearPlayers();

        dataThreadPool.execute(() -> {
            Main.getDatabase().clearOldRows("mutes");
            Main.getDatabase().clearOldRows("bans");
            Main.getDatabase().clearOldRows("warns");

            try {
                Main.getDatabase().getSQLConnection().close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            dataThreadPool.shutdown();
        });

        info("✔ Plugin disabled");
    }
}
