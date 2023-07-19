package net.flectone.integrations.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.Main;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public FExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "FlectoneChat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheFaser";
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
    public String onRequest(OfflinePlayer player, String params) {

        if(params.equalsIgnoreCase("stream_prefix")){

            if(player == null || !player.isOnline()) return "";

            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            return ObjectUtil.translateHexToColor(fPlayer.getStreamPrefix());
        }

        if(params.equalsIgnoreCase("afk_suffix")) {

            if(player == null || !player.isOnline()) return "";

            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            return ObjectUtil.translateHexToColor(fPlayer.getAfkSuffix());
        }

        if(params.equalsIgnoreCase("world_prefix")){
            if(player == null || !player.isOnline()) return "";

            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            return ObjectUtil.translateHexToColor(fPlayer.getWorldPrefix());
        }

        if(params.equalsIgnoreCase("player_name")){
            if(player == null || !player.isOnline()) return "";

            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            return fPlayer.getName();
        }

        return null; // Placeholder is unknown by the Expansion
    }
}
