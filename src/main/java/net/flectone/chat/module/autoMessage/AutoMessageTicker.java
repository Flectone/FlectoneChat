package net.flectone.chat.module.autoMessage;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.PlayerUtil;

import static net.flectone.chat.manager.FileManager.config;

public class AutoMessageTicker extends FTicker {

    public AutoMessageTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = config.getInt("default." + getModule() + ".period");
        super.delay = period;
        runTaskTimer();
    }

    @Override
    public void run() {
        PlayerUtil.getPlayersWithFeature(getModule() + ".enable")
                .stream()
                .filter(player -> getModule().isEnabledFor(player))
                .forEach(player -> ((AutoMessageModule) getModule()).send(player));
    }
}
