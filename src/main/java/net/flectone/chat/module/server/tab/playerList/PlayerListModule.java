package net.flectone.chat.module.server.tab.playerList;

import net.flectone.chat.module.FModule;

public class PlayerListModule extends FModule {
    public PlayerListModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;

        actionManager.add(new PlayerListTicker(this));
    }
}
