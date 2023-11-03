package net.flectone.chat.module.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.name.NameModule;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FPlaceholderAPI extends PlaceholderExpansion implements FIntegration {

    public FPlaceholderAPI() {
        init();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "FlectoneChat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheFaser, fxd";
    }

    @Override
    public @NotNull String getVersion() {
        return FlectoneChat.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return null;

        FPlayer fPlayer = FPlayerManager.get(player.getName());
        if(fPlayer == null) return null;

        String placeholder = switch (params.toLowerCase()) {
            case "stream_prefix" -> fPlayer.getStreamPrefix();
            case "afk_suffix" -> fPlayer.getAfkSuffix();
            case "world_prefix" -> fPlayer.getWorldPrefix();
            default -> null;
        };

        FModule fModule = FlectoneChat.getModuleManager().get(NameModule.class);
        if (!(fModule instanceof NameModule nameModule)) return placeholder;
        Player onlinePlayer = fPlayer.getPlayer();
        if (onlinePlayer == null) return placeholder;

        return switch (params.toLowerCase()) {
            case "player_name_real" -> nameModule.getReal(onlinePlayer);
            case "player_name_display" -> nameModule.getDisplay(onlinePlayer);
            case "player_name_tab" -> nameModule.getTab(onlinePlayer);
            case "player_suffix" -> nameModule.getSuffix(onlinePlayer);
            case "player_prefix" -> nameModule.getPrefix(onlinePlayer);
            default -> null;
        };
    }

    @Override
    public void init() {
        register();
        FlectoneChat.info("PlaceholderAPI detected and hooked");
    }
}
