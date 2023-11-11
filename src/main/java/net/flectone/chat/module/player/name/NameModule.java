package net.flectone.chat.module.player.name;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NameModule extends FModule {

    public NameModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @NotNull
    public String getPrefix(@NotNull Player player) {
        if (!isEnabledFor(player)) return "";

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return "";

        String prefix = config.getVaultString(player, this + ".prefix");

        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, prefix));
    }

    @NotNull
    public String getSuffix(@NotNull Player player) {
        if (!isEnabledFor(player)) return "";

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return "";

        String suffix = config.getVaultString(player, this + ".suffix");

        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, suffix));
    }

    @NotNull
    public String getReal(@NotNull Player player) {
        if (!isEnabledFor(player)) return "";

        String playerName = player.getName();

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return playerName;

        String configString = config.getVaultString(player, this + ".real");

        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, configString));
    }

    @NotNull
    public String getDisplay(@NotNull Player player) {
        if (!isEnabledFor(player)) return "";

        String playerName = player.getName();

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return playerName;

        String configString = config.getVaultString(player, this + ".display");

        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, configString));
    }

    @NotNull
    public String getTab(@NotNull Player player) {
        if (!isEnabledFor(player)) return "";

        String playerName = player.getName();

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return playerName;

        String configString = config.getVaultString(player, this + ".tab");

        return MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, configString));
    }
}
