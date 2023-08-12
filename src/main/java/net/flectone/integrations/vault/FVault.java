package net.flectone.integrations.vault;

import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class FVault implements HookInterface {

    private static boolean isEnable = false;

    private static Chat provider;

    @NotNull
    public static Chat getProvider() {
        return provider;
    }

    public static boolean isEnable() {
        return isEnable;
    }

    @Override
    public void hook() {
        RegisteredServiceProvider<Chat> chatProvider = Main.getInstance().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.chat.Chat.class);

        if (chatProvider == null) {
            Main.warning("⚠ Failed to load Vault Chat, you may not have LuckPerms installed");
            return;
        }

        provider = chatProvider.getProvider();
        isEnable = true;

        Main.info("\uD83D\uDD12 Vault detected and hooked");
    }
}
