package net.flectone.chat.module.playerMessage.book;


import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FModule;

public class BookModule extends FModule {
    public BookModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new BookListener(this));
    }
}
