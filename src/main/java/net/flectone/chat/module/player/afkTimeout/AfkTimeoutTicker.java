package net.flectone.chat.module.player.afkTimeout;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.PlayerUtil;

public class AfkTimeoutTicker extends FTicker {
    public AfkTimeoutTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = 20;
        runTaskTimer();
    }

    @Override
    public void run() {
        PlayerUtil.getPlayersWithFeature(getModule() + ".enable")
                .stream()
                .filter(player -> !getModule().hasNoPermission(player))
                .filter(player -> getModule().isEnabledFor(player))
                .forEach(player -> ((AfkTimeoutModule) getModule()).checkAfk(player));
    }
}
