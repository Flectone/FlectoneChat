package net.flectone.chat.module.extra.spit;

import net.flectone.chat.model.spit.Spit;
import net.flectone.chat.module.FModule;
import org.bukkit.entity.Player;


public class SpitModule extends FModule {

    public SpitModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new SpitListener(this));
    }

    public void spit(Player player) {
        new Spit(player, this.toString()).spawn();
    }
}
