package net.flectone.chat.module.extra;

import lombok.Getter;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.extra.itemSign.ItemSignModule;
import net.flectone.chat.module.extra.knocking.KnockingModule;
import net.flectone.chat.module.extra.mark.MarkModule;
import net.flectone.chat.module.extra.spit.SpitModule;

@Getter
public class ExtraModule extends FModule {

    private KnockingModule knockingModule;
    private SpitModule spitModule;
    private MarkModule markModule;
    private ItemSignModule itemSignModule;

    public ExtraModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        knockingModule = new KnockingModule(this, "knocking");
        spitModule = new SpitModule(this, "spit");
        markModule = new MarkModule(this, "mark");
        itemSignModule = new ItemSignModule(this, "item-sign");
    }
}
