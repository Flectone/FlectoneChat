package net.flectone.chat.module.player.world;

import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.flectone.chat.manager.FileManager.config;

public class WorldModule extends FModule {

    public WorldModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new WorldListener(this));
    }

    @NotNull
    public String getPrefix(@NotNull Player player, @NotNull World world) {
        String worldType = config.getVaultString(player, this + ".mode").equals("type")
                ? world.getEnvironment().toString().toLowerCase()
                : world.getName().toLowerCase();

        String prefix = config.getVaultString(player, this + "." + worldType);
        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, prefix));
    }
}
