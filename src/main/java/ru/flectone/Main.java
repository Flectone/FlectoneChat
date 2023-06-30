package ru.flectone;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.flectone.commands.*;
import ru.flectone.custom.FCommands;
import ru.flectone.custom.FPlayer;
import ru.flectone.listeners.FActions;
import ru.flectone.listeners.FChat;
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

    public static FileResource mails;

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

        mails = new FileResource("mails.yml");

        PlayerUtils.setOnlinePlayers(new HashMap<>());

        registerEvents(new FActions());
        registerEvents(new FChat());

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
        setCommandExecutor(new CommandsMailClear(), "mail-clear");
        setCommandExecutor(new CommandFlectonechat(), "flectonechat");
        setCommandExecutor(new CommandAfk(), "afk");

        for(Player playerOnline : Bukkit.getOnlinePlayers()){
            new FPlayer(playerOnline);
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Utils.isHavePAPI = true;
        }

        startTabScheduler();
        startPlayerMoveTimer();

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
                        FPlayer fPlayer = PlayerUtils.getPlayer(player);
                        fPlayer.setPlayerListHeaderFooter();
                    });

                }
            };

            tabTimer.runTaskTimer(Main.getInstance(), 0L, 20L * Main.config.getInt("tab.update.rate"));
        }
    }

    private static BukkitRunnable playerMoveTimer;

    public void checkPlayerMoveTimer(){
        boolean isEnable = Main.config.getBoolean("afk.timeout.enable");

        if(isEnable && (playerMoveTimer.isCancelled() || playerMoveTimer == null)){
            startPlayerMoveTimer();
        } else if (!isEnable && playerMoveTimer != null && !playerMoveTimer.isCancelled()){
            playerMoveTimer.cancel();
        }
    }

    public void startPlayerMoveTimer(){

        if(!Main.config.getBoolean("afk.timeout.enable")) return;

        playerMoveTimer = new BukkitRunnable() {
            @Override
            public void run() {

                PlayerUtils.onlinePlayers.values().forEach(fPlayer -> {
                    Block block = fPlayer.getPlayer().getLocation().getBlock();

                    if(!fPlayer.isMoved(block)){

                        if(fPlayer.isAfk()) return;

                        int diffTime = ((int) System.currentTimeMillis()/1000) - fPlayer.getLastTimeMoved();

                        if(diffTime >= Main.config.getInt("afk.timeout.time")){
                            CommandAfk.sendMessage(fPlayer, true);
                        }

                        return;
                    }

                    fPlayer.setLastBlock(block);

                    if(!fPlayer.isAfk()) return;

                    CommandAfk.sendMessage(fPlayer, false);
                });
            }
        };

        playerMoveTimer.runTaskTimer(Main.getInstance(), 0L, 20L);

    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }

    private void registerEvents(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private final TabComplets tabComplets = new TabComplets();

    private void setCommandExecutor(CommandExecutor commandExecutor, String command){
        getCommand(command).setExecutor(commandExecutor);
        getCommand(command).setTabCompleter(tabComplets);
    }
}
