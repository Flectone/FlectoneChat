package net.flectone.integrations.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FPlaceholderAPI extends PlaceholderExpansion implements HookInterface {

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
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return null;

        FPlayer fPlayer = FPlayerManager.getPlayer(player);

        if(fPlayer == null) return null;

        return switch (params.toLowerCase()) {
            case "stream_prefix" -> ObjectUtil.translateHexToColor(fPlayer.getStreamPrefix());
            case "afk_suffix" -> ObjectUtil.translateHexToColor(fPlayer.getAfkSuffix());
            case "world_prefix" -> ObjectUtil.translateHexToColor(fPlayer.getWorldPrefix());
            case "player_display_name" -> fPlayer.getDisplayName();
            case "player_tab_name" -> fPlayer.getTabName();
            default -> null;
        };
    }

    @Override
    public void hook() {
        register();
        HookManager.enabledPlaceholderAPI = true;
        Main.info("\uD83D\uDD12 PlaceholderAPI detected and hooked");
    }
}
