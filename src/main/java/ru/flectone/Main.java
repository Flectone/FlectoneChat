package ru.flectone;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.flectone.chat.FChat;
import ru.flectone.commands.Commands;
import ru.flectone.commands.TabComplets;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.Metrics;
import ru.flectone.utils.PlayerUtils;
import ru.flectone.utils.Utils;

import java.util.HashMap;

public final class Main extends JavaPlugin {
    private static Main instance;

    public static FileResource config;

    public static FileResource locale;

    public static FileResource ignores;

    public static FileResource themes;

    public static Main getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 16733);

        instance = this;

        config = new FileResource("config.yml");
        locale = new FileResource("language/" + config.getString("language") + ".yml");
        ignores = new FileResource("ignores.yml");
        themes = new FileResource("themes.yml");

        PlayerUtils.setOnlinePlayers(new HashMap<>());

        registerEvents(new FActions());
        registerEvents(new FChat());

        setExecutor("ignore");
        setExecutor("ignore-list");
        setExecutor("msg");
        setExecutor("try-cube");
        setExecutor("try");
        setExecutor("reply");
        setExecutor("me");
        setExecutor("chatcolor");
        setExecutor("flectonechat");
        setExecutor("stream");
        setExecutor("ping");
        setExecutor("mark");
        setExecutor("lastonline");
        setExecutor("firstonline");

        for(Player playerOnline : Bukkit.getOnlinePlayers()){
            new FPlayer(playerOnline);
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Utils.isHavePAPI = true;
        }

        if(Main.config.getBoolean("tab.update.enable")) {

            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
                FPlayer fPlayer = PlayerUtils.getPlayer(player);
                fPlayer.setPlayerListHeaderFooter();
            }), 0L, 20L * Main.config.getInt("tab.update.rate"));

        }

        getLogger().info("Enabled");

    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }

    private void registerEvents(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void setExecutor(String command){
        getCommand(command).setExecutor(new Commands());
        getCommand(command).setTabCompleter(new TabComplets());
    }
}
