package net.flectone.integrations.vault;

import net.flectone.Main;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

public class FVault {

    public static boolean registered = false;

    private static Chat provider;

    public static void register() {
        RegisteredServiceProvider<Chat> chatProvider = Main.getInstance().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.chat.Chat.class);

        if (chatProvider == null) {
            Main.warning("⚠ Failed to load Vault Chat, you may not have LuckPerms installed");
            return;
        }

        provider = chatProvider.getProvider();
        registered = true;
    }

    @Nullable
    public static Chat getProvider() {
        return provider;
    }

}
