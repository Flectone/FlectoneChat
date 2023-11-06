package net.flectone.chat.module.color;

import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ColorModule extends FModule {

    public ColorModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Nullable
    public HashMap<String, String> getColors(@NotNull Player player) {
        if (!isEnabledFor(player)) return null;

        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer != null) {
            if (fPlayer.getSettings() != null)
                return fPlayer.getSettings().getColors();
        }
        return null;
    }
}
