package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.misc.runnables.FBukkitRunnable;

public class DatabaseTicker extends FBukkitRunnable {

    public DatabaseTicker() {
        super.delay = 20L * 3600 * 5;
        super.period = 20L * 3600 * 5;
    }

    @Override
    public void run() {
        Main.getDatabase().delayedUpdateDatabase();
    }
}
