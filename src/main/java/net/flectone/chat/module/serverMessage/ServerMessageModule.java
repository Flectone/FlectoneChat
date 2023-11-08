package net.flectone.chat.module.serverMessage;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.serverMessage.advancement.AdvancementModule;
import net.flectone.chat.module.serverMessage.death.DeathModule;
import net.flectone.chat.module.serverMessage.join.JoinModule;
import net.flectone.chat.module.serverMessage.quit.QuitModule;

public class ServerMessageModule extends FModule {

    public ServerMessageModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        new JoinModule(this, "join");
        new QuitModule(this, "quit");
        new AdvancementModule(this, "advancement");
        new DeathModule(this, "death");
    }
}
