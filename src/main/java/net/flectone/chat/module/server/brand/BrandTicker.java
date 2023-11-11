package net.flectone.chat.module.server.brand;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.PlayerUtil;

public class BrandTicker extends FTicker {

    public BrandTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        if (!config.getBoolean("default." + getModule() + ".update.enable")) return;

        super.period = config.getInt("default." + getModule() + ".update.rate");
        runTaskTimer();
    }

    @Override
    public void run() {
        PlayerUtil.getPlayersWithFeature(getModule() + ".enable")
                .stream()
                .filter(player -> !getModule().hasNoPermission(player))
                .filter(player -> getModule().isEnabledFor(player))
                .forEach(player ->
                        ((BrandModule) getModule()).updateBrand(player,
                                ((BrandModule) getModule()).incrementIndexAndGet(player)));
    }
}
