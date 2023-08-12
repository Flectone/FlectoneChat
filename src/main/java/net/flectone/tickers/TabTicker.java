package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import org.bukkit.Bukkit;

import static net.flectone.managers.FileManager.config;

public class TabTicker extends FBukkitRunnable {

    public TabTicker() {
        super.period = 20L * config.getInt("tab.update.rate");
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) return;
            fPlayer.setPlayerListHeaderFooter();
        });
    }
}
