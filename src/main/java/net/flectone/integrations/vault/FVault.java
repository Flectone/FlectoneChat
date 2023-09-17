package net.flectone.integrations.vault;

import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.managers.HookManager;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FVault implements HookInterface {

    private static Chat provider;

    @NotNull
    public static Chat getProvider() {
        return provider;
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
        HookManager.enabledVault = true;

        Main.info("\uD83D\uDD12 Vault detected and hooked");
    }

    @Nullable
    public static String getPrimaryGroup(Player player) {
        return FVault.getProvider().getPrimaryGroup(player);
    }
}
