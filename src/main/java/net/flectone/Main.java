package net.flectone;

import net.flectone.commands.*;
import net.flectone.custom.FPlayer;
import net.flectone.custom.FTabCompleter;
import net.flectone.integrations.expansions.FExpansion;
import net.flectone.listeners.*;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.FileManager;
import net.flectone.sqlite.Database;
import net.flectone.sqlite.SQLite;
import net.flectone.utils.MetricsUtil;
import net.flectone.utils.ObjectUtil;
import net.flectone.integrations.voicechats.simplevoicechat.RegisterSimpleVoiceChat;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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

        registerEvents();
        setCommandExecutors();

        hookPlugins();

        startTabScheduler();
        checkPlayerMoveTimer();

        info("✔ Plugin enabled");
    }

    public static void info(String message){
        getInstance().getLogger().info(message);
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
        FPlayerManager.removePlayersFromTeams();
        info("✔ Plugin disabled");
    }

    private void registerEvents(){
        registerEvent(new AsyncPlayerChatListener());
        registerEvent(new EntitySpawnListener());
        registerEvent(new InventoryClickListener());
        registerEvent(new InventoryOpenListener());
        registerEvent(new PlayerChangeWorldListener());
        registerEvent(new PlayerCommandPreprocessListener());
        registerEvent(new PlayerInteractListener());
        registerEvent(new PlayerJoinListener());
        registerEvent(new PlayerQuitListener());
        registerEvent(new ServerListPingListener());
    }

    private void registerEvent(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void setCommandExecutors(){
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
        setCommandExecutor(new CommandBall(), "ball");
        setCommandExecutor(new CommandTicTacToe(), "tic-tac-toe");
        setCommandExecutor(new CommandClearChat(), "clear-chat");
        setCommandExecutor(new CommandTempban(), "tempban");
        setCommandExecutor(new CommandUnban(), "unban");
    }

    private void setCommandExecutor(FTabCompleter commandExecutor, String command){
        getCommand(command).setExecutor(commandExecutor);
        getCommand(command).setTabCompleter(commandExecutor);
    }
}
