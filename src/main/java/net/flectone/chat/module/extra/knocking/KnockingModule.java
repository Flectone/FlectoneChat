package net.flectone.chat.module.extra.knocking;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.sounds.SoundsModule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KnockingModule extends FModule {
    public KnockingModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new KnockingListener(this));
    }

    public void knock(@NotNull Player player, @NotNull Location location, @NotNull String name)  {
        FModule fModule = FlectoneChat.getModuleManager().get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, location, name));
        }
    }
}
