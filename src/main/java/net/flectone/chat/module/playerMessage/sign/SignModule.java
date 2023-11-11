package net.flectone.chat.module.playerMessage.sign;

import net.flectone.chat.module.FModule;

public class SignModule extends FModule {

    public SignModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new SignListener(this));
    }
}
