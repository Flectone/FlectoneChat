package net.flectone.chat.module.extra.knocking;

import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
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
        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer == null) return;

        fPlayer.playSound(location, name);
    }
}
