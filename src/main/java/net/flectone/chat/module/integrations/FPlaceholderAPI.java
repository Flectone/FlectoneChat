package net.flectone.chat.module.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.util.TimeUtil;
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
        if (player == null) return null;

        FPlayer fPlayer = FPlayerManager.getOffline(player.getName());
        if(fPlayer == null) return null;

        String placeholder = switch (params.toLowerCase()) {

        if (params.startsWith("moderation")) {
            FlectoneChat.getDatabase().getWarns(fPlayer);

            return switch (params.toLowerCase()) {
                case "moderation_ban" -> FPlayerManager.getBANNED_PLAYERS().contains(fPlayer.getUuid()) ? "1" : "0";
                case "moderation_mute" -> FPlayerManager.getMUTED_PLAYERS().contains(fPlayer.getMinecraftName()) ? "1" : "0";
                case "moderation_warn" -> String.valueOf(fPlayer.getCountWarns());
                default -> null;
            };
        }

        return switch (params.toLowerCase()) {
            case "lastonline" -> TimeUtil.convertTime(fPlayer.getPlayer(), player.getLastPlayed());
            case "firstonline" -> TimeUtil.convertTime(fPlayer.getPlayer(), player.getFirstPlayed());
            case "stream_prefix" -> fPlayer.getStreamPrefix();
            case "afk_suffix" -> fPlayer.getAfkSuffix();
            case "world_prefix" -> fPlayer.getWorldPrefix();
            default -> null;
        };

        if (placeholder != null) return placeholder;

        FModule fModule = FlectoneChat.getModuleManager().get(NameModule.class);
        if (!(fModule instanceof NameModule nameModule)) return null;
        Player onlinePlayer = fPlayer.getPlayer();
        if (onlinePlayer == null) return null;

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
