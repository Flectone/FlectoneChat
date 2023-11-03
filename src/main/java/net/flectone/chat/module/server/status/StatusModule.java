package net.flectone.chat.module.server.status;

import net.flectone.chat.manager.FActionManager;
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

        FActionManager.add(new StatusListener(this));
    }
}
