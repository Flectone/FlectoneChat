package net.flectone.chat.module.integrations;

import net.flectone.chat.FlectoneChat;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FVault implements FIntegration {


    private static Chat provider;

    public FVault() {
        init();
    }

    @Override
    public void init() {
        RegisteredServiceProvider<Chat> chatProvider = FlectoneChat.getPlugin().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.chat.Chat.class);

        if (chatProvider == null) {
            FlectoneChat.warning("Failed to load Vault Chat, you may not have LuckPerms installed");
            return;
        }

        provider = chatProvider.getProvider();

        FlectoneChat.info("Vault detected and hooked");
    }

    @Nullable
    public String getPrimaryGroup(Player player) {
        return provider.getPrimaryGroup(player);
    }

    @Nullable
    public String getPrefix(@NotNull Player player) {
        return provider.getPlayerPrefix(player);
    }

    @Nullable
    public String getSuffix(@NotNull Player player) {
        return provider.getPlayerSuffix(player);
    }

    public boolean isEnabled() {
        return provider != null;
    }
}
