package net.flectone.managers;

import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.expansions.FPlaceholderAPI;
import net.flectone.integrations.interactivechat.FInteractiveChat;
import net.flectone.integrations.luckperms.FLuckPerms;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.integrations.vault.FVault;
import net.flectone.integrations.voicechats.plasmovoice.FPlasmoVoice;
import net.flectone.integrations.voicechats.simplevoicechat.FSimpleVoiceChat;
import org.bukkit.Bukkit;

public class HookManager {

    public static boolean enabledDiscordSRV = false;
    public static boolean enabledInteractiveChat = false;
    public static boolean enabledPlaceholderAPI = false;
    public static boolean enabledPlasmoVoice = false;
    public static boolean enabledVault = false;
    public static boolean enabledLuckPerms = false;

    public static void hookPlugins() {
        if (isEnabled("DiscordSRV")) new FDiscordSRV().hook();
        if (isEnabled("PlaceholderAPI")) new FPlaceholderAPI().hook();
        if (isEnabled("InteractiveChat")) new FInteractiveChat().hook();
        if (isEnabled("LuckPerms")) new FLuckPerms().hook();
        if (isEnabled("SuperVanish")) new FSuperVanish().hook();
        if (isEnabled("Vault")) new FVault().hook();
        if (isEnabled("plasmovoice")) new FPlasmoVoice().hook();
        if (isEnabled("voicechat")) new FSimpleVoiceChat().hook();
    }

    private static boolean isEnabled(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }
}
