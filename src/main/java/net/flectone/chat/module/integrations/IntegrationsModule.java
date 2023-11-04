package net.flectone.chat.module.integrations;

import me.clip.placeholderapi.PlaceholderAPI;
import net.flectone.chat.module.FModule;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static net.flectone.chat.manager.FileManager.integrations;


public class IntegrationsModule extends FModule {

    private static final HashMap<String, FIntegration> INTEGRATIONS_MAP = new HashMap<>();

    public IntegrationsModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        if (isPluginEnabled("DiscordSRV")) {
            INTEGRATIONS_MAP.put("DiscordSRV", new FDiscordSRV());
        }
        if (isPluginEnabled("PlaceholderAPI")) {
            INTEGRATIONS_MAP.put("PlaceholderAPI", new FPlaceholderAPI());
        }
        if (isPluginEnabled("InteractiveChat")) {
            INTEGRATIONS_MAP.put("InteractiveChat", new FInteractiveChat());
        }
        if (isPluginEnabled("LuckPerms")) {
            INTEGRATIONS_MAP.put("LuckPerms", new FLuckPerms());
        }
        if (isPluginEnabled("SuperVanish")) {
            INTEGRATIONS_MAP.put("SuperVanish", new FSuperVanish());
        }
        if (isPluginEnabled("Vault")) {
            INTEGRATIONS_MAP.put("Vault", new FVault());
        }
        if (isPluginEnabled("PlasmoVoice")) {
            INTEGRATIONS_MAP.put("PlasmoVoice", new FPlasmoVoice());
        }
        if (isPluginEnabled("VoiceChat")) {
            INTEGRATIONS_MAP.put("VoiceChat", new FSimpleVoiceChat());
        }
    }

    private static FIntegration get(String integration) {
        return INTEGRATIONS_MAP.get(integration);
    }

    private boolean isPluginEnabled(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null
                && integrations.getBoolean(plugin + ".enable");
    }

    public static boolean isVanished(@NotNull Player player) {
        return player.getMetadata("vanished").parallelStream()
                .anyMatch(MetadataValue::asBoolean);
    }

    @Nullable
    public static String[] getGroups(@Nullable Player player) {
        if (player == null) return null;
        FIntegration luckPerms = get("LuckPerms");
        if (luckPerms != null) return ((FLuckPerms) luckPerms).getGroups(player);
        FIntegration vault = get("Vault");
        if (vault != null) return new String[]{((FVault) vault).getPrimaryGroup(player)};
        return null;
    }

    public static int getPrimaryGroupWeight(@NotNull Player player) {
        FIntegration fIntegration = get("LuckPerms");
        if (fIntegration == null) return 0;
        return ((FLuckPerms) fIntegration).getPrimaryGroupWeight(player);
    }

    public static String interactiveChatMark(@NotNull String string, @NotNull UUID uuid) {
        FIntegration interactiveChat = get("InteractiveChat");
        if (interactiveChat == null) return string;
        return ((FInteractiveChat) interactiveChat).mark(string, uuid);
    }

    public static String interactiveChatCheckMention(@NotNull AsyncPlayerChatEvent event) {
        FIntegration interactiveChat = get("InteractiveChat");
        if (interactiveChat == null) return event.getMessage();
        return ((FInteractiveChat) interactiveChat).checkMention(event);
    }

    public static String replaceInteractiveChatPlaceholders(@NotNull String string) {
        FIntegration interactiveChat = get("InteractiveChat");
        if (interactiveChat == null) return string;

        return string.replaceAll("(<chat=.*>)", "[...]");
    }

    @Nullable
    public static String getPrefix(@NotNull Player player) {
        FIntegration luckPerms = get("LuckPerms");
        if (luckPerms != null) return ((FLuckPerms) luckPerms).getPrefix(player);
        FIntegration vault = get("Vault");
        if (vault != null) return ((FVault) vault).getPrefix(player);
        return null;
    }

    @Nullable
    public static String getSuffix(@NotNull Player player) {
        FIntegration luckPerms = get("LuckPerms");
        if (luckPerms != null) return ((FLuckPerms) luckPerms).getSuffix(player);
        FIntegration vault = get("Vault");
        if (vault != null) return ((FVault) vault).getSuffix(player);
        return null;
    }

    @Nullable
    public static String getPrimaryGroup(@NotNull Player player) {
        FIntegration luckPerms = get("LuckPerms");
        if (luckPerms != null) return ((FLuckPerms) luckPerms).getPrimaryGroup(player);
        FIntegration vault = get("Vault");
        if (vault != null) return ((FVault) vault).getPrimaryGroup(player);
        return null;
    }

    @NotNull
    public static String setPlaceholders(@Nullable OfflinePlayer sender, @Nullable OfflinePlayer recipient,
                                         @NotNull String string) {

        if (get("PlaceholderAPI") == null) return string;

        string = PlaceholderAPI.setPlaceholders(sender, string);

        if (!(sender instanceof Player pSender && recipient instanceof  Player pRecipient)) return string;

        return PlaceholderAPI.setRelationalPlaceholders(pSender, pRecipient, string);
    }

    public static void unregister() {
        FIntegration placeholderAPI = INTEGRATIONS_MAP.get("PlaceholderAPI");
        if (placeholderAPI != null) ((FPlaceholderAPI) placeholderAPI).unregister();

        FIntegration discordSRV = INTEGRATIONS_MAP.get("DiscordSRV");
        if (discordSRV != null) ((FDiscordSRV) discordSRV).unsubscribe();
    }
}
