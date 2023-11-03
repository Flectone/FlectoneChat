package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class FTicker extends BukkitRunnable implements FAction {

    protected long delay = 0L;
    protected long period;

    @Getter
    private FModule module;

    public FTicker(FModule module) {
        this.module = module;
    }

    @Override
    public void run() {
    }

    public void runTaskTimer() {
        super.runTaskTimer(FlectoneChat.getInstance(), delay, period);
    }
}
