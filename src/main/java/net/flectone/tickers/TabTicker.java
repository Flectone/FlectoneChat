package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.misc.entity.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;

public class TabTicker extends FBukkitRunnable {

    public TabTicker() {
        super.period = 20L * Main.config.getInt("tab.update.rate");
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            fPlayer.setPlayerListHeaderFooter();
        });
    }
}
