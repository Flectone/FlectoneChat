package net.flectone.integrations.luckperms;

import net.flectone.Main;
import net.flectone.misc.entity.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;

public class FLuckPerms {

    public FLuckPerms(Main plugin) {

        LuckPerms luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();

        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(plugin, NodeAddEvent.class, this::onUserPromote);
    }

    private void onUserPromote(NodeMutateEvent event) {
        if (event.getTarget() instanceof User) {
            FPlayer fPlayer = FPlayerManager.getPlayer(((User) event.getTarget()).getUniqueId());
            fPlayer.setStreamer();
            fPlayer.setDisplayName();
            return;
        }

        Bukkit.getOnlinePlayers().parallelStream()
                .forEach(player -> {
                    FPlayer fPlayer = FPlayerManager.getPlayer(player);
                    fPlayer.setStreamer();
                    fPlayer.setDisplayName();
                });
    }
}
