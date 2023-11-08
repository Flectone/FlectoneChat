package net.flectone.chat.module.player;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.afkTimeout.AfkTimeoutModule;
import net.flectone.chat.module.player.hover.HoverModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.module.player.nameTag.NameTagModule;
import net.flectone.chat.module.player.rightClick.RightClickModule;
import net.flectone.chat.module.player.world.WorldModule;

public class PlayerModule extends FModule {

    public PlayerModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        new NameTagModule(this, "name-tag");
        new RightClickModule(this, "right-click");
        new HoverModule(this, "hover");
        new NameModule(this, "name");
        new WorldModule(this, "world");
        new AfkTimeoutModule(this, "afk-timeout");
    }
}
