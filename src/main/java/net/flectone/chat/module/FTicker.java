package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class FTicker extends BukkitRunnable implements FAction {

    protected long delay = 0L;
    protected long period;

    @Getter
    private FModule module;

    protected final FConfiguration config;
    protected final FConfiguration locale;
    protected final FPlayerManager playerManager;

    public FTicker(FModule module) {
        this.module = module;

        FlectoneChat plugin = FlectoneChat.getPlugin();
        config = plugin.getFileManager().getConfig();
        locale = plugin.getFileManager().getLocale();
        playerManager = plugin.getPlayerManager();
    }

    @Override
    public void run() {
    }

    public void runTaskTimer() {
        super.runTaskTimer(FlectoneChat.getPlugin(), delay, period);
    }
}
