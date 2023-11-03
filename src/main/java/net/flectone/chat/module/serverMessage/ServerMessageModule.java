package net.flectone.chat.module.serverMessage;

import lombok.Getter;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.serverMessage.advancement.AdvancementModule;
import net.flectone.chat.module.serverMessage.death.DeathModule;
import net.flectone.chat.module.serverMessage.join.JoinModule;
import net.flectone.chat.module.serverMessage.quit.QuitModule;

@Getter
public class ServerMessageModule extends FModule {

    private JoinModule joinModule;
    private QuitModule quitModule;
    private AdvancementModule advancementModule;
    private DeathModule deathModule;

    public ServerMessageModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        joinModule = new JoinModule(this, "join");
        quitModule = new QuitModule(this, "quit");
        advancementModule = new AdvancementModule(this, "advancement");
        deathModule = new DeathModule(this, "death");
    }
}
