package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class PlayerClearTicker extends FBukkitRunnable {

    public PlayerClearTicker() {
        super.period = 20L * 60 * 60;
        super.delay = 20L * 60 * 60;
    }

    @Override
    public void run() {
        HashMap<String, FPlayer> fPlayerHashMap = new HashMap<>();

        Bukkit.getOnlinePlayers().forEach(player ->
                fPlayerHashMap.put(player.getName(), FPlayerManager.getPlayer(player)));

        FPlayerManager.getUsedFPlayers().clear();
        FPlayerManager.getUsedFPlayers().putAll(fPlayerHashMap);
    }
}
