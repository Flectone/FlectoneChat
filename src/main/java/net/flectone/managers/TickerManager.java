package net.flectone.managers;

import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.tickers.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;

public class TickerManager {

    private static final List<FBukkitRunnable> bukkitRunnableList = new ArrayList<>();

    private static void addTicker(FBukkitRunnable bukkitRunnable) {
        bukkitRunnableList.add(bukkitRunnable);
    }

    public static void clear() {
        bukkitRunnableList.parallelStream().forEach(BukkitRunnable::cancel);
        bukkitRunnableList.clear();
    }

    public static void start() {

        if (config.getBoolean("chat.bubble.enable")) {
            addTicker(new ChatBubbleTicker());
        }

        if (config.getBoolean("command.afk.timeout.enable")) {
            addTicker(new AfkTicker());
        }

        if (config.getBoolean("tab.update.enable")) {
            addTicker(new TabTicker());
        }

        if (config.getBoolean("tab.player-ping.enable")) {
            PlayerPingTicker.registerPingObjective();
            addTicker(new PlayerPingTicker());
        } else PlayerPingTicker.unregisterPingObjective();

        if (config.getBoolean("server.brand.enable") && config.getBoolean("server.brand.update.enable")) {
            addTicker(new ServerBrandTicker());
        }

        if (config.getBoolean("chat.auto-message.enable")) {
            addTicker(new AutoMessageTicker());
        }

        addTicker(new PlayerClearTicker());

        bukkitRunnableList.parallelStream().forEach(FBukkitRunnable::runTaskTimer);
    }
}
