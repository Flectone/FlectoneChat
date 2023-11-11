package net.flectone.chat.module.player.world;

import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldModule extends FModule {

    public WorldModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new WorldListener(this));
    }

    @NotNull
    public String getPrefix(@NotNull Player player, @NotNull World world) {
        if (!isEnabledFor(player)) return "";

        String worldType = config.getVaultString(player, this + ".mode").equals("type")
                ? world.getEnvironment().toString().toLowerCase()
                : world.getName().toLowerCase();

        String prefix = config.getVaultString(player, this + "." + worldType);
        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, prefix));
    }
}
