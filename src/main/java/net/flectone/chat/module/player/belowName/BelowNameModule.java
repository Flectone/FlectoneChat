package net.flectone.chat.module.player.belowName;

import net.flectone.chat.module.FModule;

public class BelowNameModule extends FModule {
    public BelowNameModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new BelowNameTicker(this));
    }
}
