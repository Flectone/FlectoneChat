package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.runnables.FBukkitRunnable;

public class PlayerClearTicker extends FBukkitRunnable {

    public PlayerClearTicker() {
        super.period = 20L * 60 * 60;
        super.delay = 20L * 60 * 60;
    }

    @Override
    public void run() {
        FPlayerManager.getUsedFPlayers().forEach((s, fPlayer) -> {
            if (fPlayer.isOnline()) return;
            FPlayerManager.removePlayer(s);
        });
    }
}
