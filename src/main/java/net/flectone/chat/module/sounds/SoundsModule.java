package net.flectone.chat.module.sounds;

import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import org.jetbrains.annotations.NotNull;

public class SoundsModule extends FModule {
    public SoundsModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    public void play(@NotNull FSound sound) {
        if (hasNoPermission(sound.getSender())) return;
        sound.play();
    }
}