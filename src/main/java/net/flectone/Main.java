package net.flectone;

import net.flectone.custom.FTabCompleter;
import net.flectone.integrations.expansions.FExpansion;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.managers.TickerManager;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.utils.MetricsUtil;
import net.flectone.integrations.voicechats.simplevoicechat.RegisterSimpleVoiceChat;
import net.flectone.utils.ReflectionUtil;
import net.flectone.utils.WebUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static boolean isHavePAPI = false;

    public static boolean isHavePlasmoVoice = false;

    public static boolean isHaveVault = false;

    private static Main instance;

    public static FileManager config;

    public static FileManager locale;

    public static Main getInstance(){
        return instance;
    }

    private Database database;

    public static Database getDatabase() {
        return getInstance().database;
    }

    @Override
    public void onEnable() {
        new MetricsUtil(this, 16733);

        instance = this;

        config = new FileManager("config.yml");
        locale = new FileManager("language/" + config.getString("language") + ".yml");

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

    public static void info(String message){
        getInstance().getLogger().info(message);
    }

    public static void warning(String message){
        getInstance().getLogger().warning(message);
    }

    private void registerClasses(){
        ReflectionUtil.registerClasses("net.flectone.listeners", (fClass) ->
                Bukkit.getServer().getPluginManager().registerEvents((Listener) fClass.getDeclaredConstructor().newInstance(), this));

        ReflectionUtil.registerClasses("net.flectone.commands", (fClass) -> {
            FTabCompleter fTabCompleter = (FTabCompleter) fClass.getDeclaredConstructor().newInstance();
            PluginCommand pluginCommand = Main.getInstance().getCommand(fTabCompleter.getCommandName());

            if(pluginCommand == null) return;

            pluginCommand.setExecutor(fTabCompleter);
            pluginCommand.setTabCompleter(fTabCompleter);
        });
    }

    private void hookPlugins(){
        if(Bukkit.getPluginManager().getPlugin("Vault") != null){
            isHaveVault = true;
            getLogger().info("\uD83D\uDD12 Vault detected and hooked");
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isHavePAPI = true;
            new FExpansion(this).register();
            getLogger().info("\uD83D\uDD12 PlaceholderAPI detected and hooked");
        }

        if(Bukkit.getPluginManager().getPlugin("voicechat") != null) {
            new RegisterSimpleVoiceChat();
            getLogger().info("\uD83D\uDD12 SimpleVoiceChat detected and hooked");
        }

        if(Bukkit.getPluginManager().getPlugin("plasmovoice") != null){
            isHavePlasmoVoice = true;
            getLogger().info("\uD83D\uDD12 PlasmoVoice detected and hooked");
        }
    }

    @Override
    public void onDisable() {
        FPlayerManager.uploadPlayers();
        FPlayerManager.removePlayersFromTeams();
        info("✔ Plugin disabled");
    }
}
