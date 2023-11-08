package net.flectone.chat.module.extra;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.extra.itemSign.ItemSignModule;
import net.flectone.chat.module.extra.knocking.KnockingModule;
import net.flectone.chat.module.extra.mark.MarkModule;
import net.flectone.chat.module.extra.spit.SpitModule;

public class ExtraModule extends FModule {

    public ExtraModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        new KnockingModule(this, "knocking");
        new SpitModule(this, "spit");
        new MarkModule(this, "mark");
        new ItemSignModule(this, "item-sign");
    }
}
