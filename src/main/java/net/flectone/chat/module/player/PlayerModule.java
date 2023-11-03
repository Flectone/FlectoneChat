package net.flectone.chat.module.player;

import lombok.Getter;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.afkTimeout.AfkTimeoutModule;
import net.flectone.chat.module.player.hover.HoverModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.module.player.nameTag.NameTagModule;
import net.flectone.chat.module.player.rightClick.RightClickModule;
import net.flectone.chat.module.player.world.WorldModule;

@Getter
public class PlayerModule extends FModule {

    private NameTagModule nameTagModule;
    private RightClickModule rightClickModule;
    private HoverModule hoverModule;
    private NameModule nameModule;
    private WorldModule worldModule;
    private AfkTimeoutModule afkTimeoutModule;

    public PlayerModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        nameTagModule = new NameTagModule(this, "name-tag");
        rightClickModule = new RightClickModule(this, "right-click");
        hoverModule = new HoverModule(this, "hover");
        nameModule = new NameModule(this, "name");
        worldModule = new WorldModule(this, "world");
        afkTimeoutModule = new AfkTimeoutModule(this, "afk-timeout");
    }
}
