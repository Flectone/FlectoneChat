package net.flectone.integrations.luckperms;

import net.flectone.Main;
import net.flectone.integrations.HookInterface;
import net.flectone.integrations.vault.FVault;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.entity.FPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class FLuckPerms implements HookInterface {

    private static LuckPerms provider;

    private void onUserPromote(@NotNull NodeMutateEvent event) {
        if (event.getTarget() instanceof User) {
            FPlayer fPlayer = FPlayerManager.getPlayer(((User) event.getTarget()).getUniqueId());
            if (fPlayer == null) return;
            fPlayer.setStreamer();
            fPlayer.updateName();
            return;
        }

        Bukkit.getOnlinePlayers().parallelStream()
                .forEach(player -> {
                    FPlayer fPlayer = FPlayerManager.getPlayer(player);
                    if (fPlayer == null) return;
                    fPlayer.setStreamer();
                    fPlayer.updateName();
                });
    }

    @Override
    public void hook() {
        RegisteredServiceProvider<LuckPerms> serviceProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (serviceProvider == null) return;

        provider = serviceProvider.getProvider();
        HookManager.enabledLuckPerms = true;

        EventBus eventBus = serviceProvider.getProvider().getEventBus();

        eventBus.subscribe(Main.getInstance(), NodeAddEvent.class, this::onUserPromote);

        Main.info("\uD83D\uDD12 LuckPerms detected and hooked");
    }

    public static LuckPerms getProvider() {
        return provider;
    }

    public static int getPlayerGroupWeight(Player player) {
        String vaultGroup = FVault.getProvider().getPrimaryGroup(player);
        Group group = FLuckPerms.getProvider().getGroupManager().getGroup(vaultGroup);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }
}
