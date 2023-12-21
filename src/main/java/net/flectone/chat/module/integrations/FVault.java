package net.flectone.chat.module.integrations;

import net.flectone.chat.FlectoneChat;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FVault implements FIntegration {


    private Chat chatProvider;
    private Permission permissionProvider;


    public FVault() {
        init();
    }

    @Override
    public void init() {
        RegisteredServiceProvider<Chat> chatProvider = FlectoneChat.getPlugin().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.chat.Chat.class);
        RegisteredServiceProvider<Permission> permissionProvider = FlectoneChat.getPlugin().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (chatProvider == null || permissionProvider == null) {
            FlectoneChat.warning("Failed to load Vault Chat, you may not have LuckPerms installed");
            return;
        }

        this.chatProvider = chatProvider.getProvider();
        this.permissionProvider = permissionProvider.getProvider();

        FlectoneChat.info("Vault detected and hooked");
    }

    @Nullable
    public String getPrimaryGroup(Player player) {
        return chatProvider.getPrimaryGroup(player);
    }

    @Nullable
    public String getPrefix(@NotNull Player player) {
        return chatProvider.getPlayerPrefix(player);
    }

    public boolean hasPermission(@NotNull OfflinePlayer player, @NotNull String permission) {
        return permissionProvider.playerHas(null, player, permission);
    }

    @Nullable
    public String getSuffix(@NotNull Player player) {
        return chatProvider.getPlayerSuffix(player);
    }

    public boolean isEnabled() {
        return chatProvider != null;
    }
}
