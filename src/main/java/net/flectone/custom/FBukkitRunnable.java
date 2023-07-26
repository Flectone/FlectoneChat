package net.flectone.custom;

import net.flectone.Main;
import org.bukkit.scheduler.BukkitRunnable;

public class FBukkitRunnable extends BukkitRunnable {

    protected long delay = 0L;
    protected long period;

    @Override
    public void run() {
    }

    public void runTaskTimer() {
        super.runTaskTimer(Main.getInstance(), delay, period);
    }
}
