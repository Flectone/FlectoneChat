package net.flectone.chat.module.server.status;

import net.flectone.chat.module.FModule;

public class StatusModule extends FModule {
    public StatusModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new StatusListener(this));
    }
}
