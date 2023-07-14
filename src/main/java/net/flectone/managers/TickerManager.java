package net.flectone.managers;

import net.flectone.Main;
import net.flectone.custom.FBukkitRunnable;
import net.flectone.tickers.AfkTicker;
import net.flectone.tickers.ChatBubbleTicker;
import net.flectone.tickers.DatabaseTicker;
import net.flectone.tickers.TabTicker;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class TickerManager {

    private static final List<FBukkitRunnable> bukkitRunnableList = new ArrayList<>();

    private static void addTicker(FBukkitRunnable bukkitRunnable){
        bukkitRunnableList.add(bukkitRunnable);
    }

    public static void clear(){
        bukkitRunnableList.forEach(BukkitRunnable::cancel);
        bukkitRunnableList.clear();
    }

    public static void start(){
        addTicker(new DatabaseTicker());

        if(Main.config.getBoolean("chat.bubble.enable")){
            addTicker(new ChatBubbleTicker());
        }

        if(Main.config.getBoolean("command.afk.timeout.enable")){
            addTicker(new AfkTicker());
        }

        if(Main.config.getBoolean("tab.update.enable")) {
            addTicker(new TabTicker());
        }

        bukkitRunnableList.forEach(FBukkitRunnable::runTaskTimer);
    }
}
