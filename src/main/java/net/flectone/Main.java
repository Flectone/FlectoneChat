package net.flectone;

import net.flectone.commands.*;
import net.flectone.custom.FPlayer;
import net.flectone.custom.FTabCompleter;
import net.flectone.expansions.FExpansion;
import net.flectone.listeners.*;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.utils.MetricsUtil;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {

    public static boolean isHavePAPI = false;

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

        registerEvents(new AsyncPlayerChatListener());
        registerEvents(new EntitySpawnListener());
        registerEvents(new InventoryClickListener());
        registerEvents(new InventoryOpenListener());
        registerEvents(new PlayerChangeWorldListener());
        registerEvents(new PlayerCommandPreprocessListener());
        registerEvents(new PlayerInteractListener());
        registerEvents(new PlayerJoinListener());
        registerEvents(new PlayerQuitListener());
        registerEvents(new ServerListPingListener());

        setCommandExecutor(new CommandIgnore(), "ignore");
        setCommandExecutor(new CommandIgnoreList(), "ignore-list");
        setCommandExecutor(new CommandTryCube(), "try-cube");
        setCommandExecutor(new CommandTry(), "try");
        setCommandExecutor(new CommandMe(), "me");
        setCommandExecutor(new CommandPing(), "ping");
        setCommandExecutor(new CommandChatcolor(), "chatcolor");
        setCommandExecutor(new CommandOnline(), "firstonline");
        setCommandExecutor(new CommandOnline(), "lastonline");
        setCommandExecutor(new CommandMark(), "mark");
        setCommandExecutor(new CommandStream(), "stream");
        setCommandExecutor(new CommandMsg(), "msg");
        setCommandExecutor(new CommandReply(), "reply");
        setCommandExecutor(new CommandMail(), "mail");
        setCommandExecutor(new CommandMailClear(), "mail-clear");
        setCommandExecutor(new CommandFlectonechat(), "flectonechat");
        setCommandExecutor(new CommandAfk(), "afk");
        setCommandExecutor(new CommandMute(), "mute");
        setCommandExecutor(new CommandUnmute(), "unmute");
        setCommandExecutor(new CommandHelper(), "helper");
        setCommandExecutor(new CommandTechnicalWorks(), "technical-works");
        setCommandExecutor(new CommandSwitchChat(), "switch-chat");

        FPlayerManager.loadPlayers();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isHavePAPI = true;
            new FExpansion(this).register();
        }

        startTabScheduler();
        checkPlayerMoveTimer();

        getLogger().info("Enabled");

    }

    private static BukkitRunnable tabTimer;

    public void startTabScheduler(){

        if(Main.config.getBoolean("tab.update.enable") && (tabTimer == null || tabTimer.isCancelled())) {

            tabTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!Main.config.getBoolean("tab.update.enable")){
                        cancel();
                    }

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        FPlayer fPlayer = FPlayerManager.getPlayer(player);
                        fPlayer.setPlayerListHeaderFooter();
                    });

                }
            };

            tabTimer.runTaskTimer(Main.getInstance(), 0L, 20L * Main.config.getInt("tab.update.rate"));
        }
    }

    private static BukkitRunnable playerMoveTimer;

    public void checkPlayerMoveTimer(){
        if(playerMoveTimer == null || playerMoveTimer.isCancelled()){
            startPlayerMoveTimer();
        }
    }

    public void startPlayerMoveTimer(){
        playerMoveTimer = new BukkitRunnable() {
            @Override
            public void run() {

                FPlayerManager.getPlayers().stream().filter(FPlayer::isOnline).forEach(fPlayer -> {
                    Block block = fPlayer.getPlayer().getLocation().getBlock();

                    if(!fPlayer.isMoved(block)){

                        boolean isEnable = Main.config.getBoolean("command.afk.timeout.enable");
                        if(fPlayer.isAfk() || !isEnable) return;

                        int diffTime = ObjectUtil.getCurrentTime() - fPlayer.getLastTimeMoved();

                        if(diffTime >= Main.config.getInt("command.afk.timeout.time")){
                            CommandAfk.sendMessage(fPlayer, true);
                        }

                        return;
                    }

                    fPlayer.setBlock(block);

                    if(!fPlayer.isAfk()) return;

                    CommandAfk.sendMessage(fPlayer, false);
                });
            }
        };

        playerMoveTimer.runTaskTimer(Main.getInstance(), 0L, 20L);

    }

    @Override
    public void onDisable() {
        FPlayerManager.uploadPlayers();
        getLogger().info("Disabled");
    }

    private void registerEvents(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void setCommandExecutor(FTabCompleter commandExecutor, String command){
        getCommand(command).setExecutor(commandExecutor);
        getCommand(command).setTabCompleter(commandExecutor);
    }
}
