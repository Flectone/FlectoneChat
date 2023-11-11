package net.flectone.chat.module.playerMessage.anvil;


import net.flectone.chat.module.FModule;

public class AnvilModule extends FModule {
    public AnvilModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new AnvilListener(this));
    }
}
