package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.custom.FBukkitRunnable;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;

public class TabTicker extends FBukkitRunnable {

    public TabTicker(){
        super.period = 20L * Main.config.getInt("tab.update.rate");
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            fPlayer.setPlayerListHeaderFooter();
        });
    }
}
