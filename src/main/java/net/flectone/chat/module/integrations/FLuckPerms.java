package net.flectone.chat.module.integrations;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
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
import org.jetbrains.annotations.Nullable;

public class FLuckPerms implements FIntegration {

    public FLuckPerms() {
        init();
    }

    @Getter
    private static LuckPerms provider;

    private void onUserPromote(@NotNull NodeMutateEvent event) {
//        if (event.getTarget() instanceof User user) {
//            if (user.getUsername() == null) return;
//            FPlayer fPlayer = FPlayerManager.getPlayer(user.getUsername());
//            if (fPlayer == null) return;
//            fPlayer.setStreamer();
//            fPlayer.updateName();
//            return;
//        }
//
//        Bukkit.getOnlinePlayers().parallelStream()
//                .forEach(player -> {
//                    FPlayer fPlayer = FPlayerManager.getPlayer(player);
//                    if (fPlayer == null) return;
//                    fPlayer.setStreamer();
//                    fPlayer.updateName();
//                });
    }

    @Override
    public void init() {
        RegisteredServiceProvider<LuckPerms> serviceProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (serviceProvider == null) return;

        provider = serviceProvider.getProvider();

        EventBus eventBus = serviceProvider.getProvider().getEventBus();
        eventBus.subscribe(FlectoneChat.getInstance(), NodeAddEvent.class, this::onUserPromote);

        FlectoneChat.info("LuckPerms detected and hooked");
    }

    public int getPlayerGroupWeight(@NotNull Player player) {
        String primaryGroup = getPrimaryGroup(player);
        if (primaryGroup == null) return 0;

        Group group = provider.getGroupManager().getGroup(primaryGroup);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }

    @Nullable
    public String getPrimaryGroup(@NotNull Player player) {
        User user = provider.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        return user.getPrimaryGroup();
    }

    @Nullable
    public String getPrefix(@NotNull Player player) {
        User user = provider.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Nullable
    public String getSuffix(@NotNull Player player) {
        User user = provider.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        return user.getCachedData().getMetaData().getSuffix();
    }

    @NotNull
    public String[] getGroups(@NotNull Player player) {
        User user = provider.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        List<String> groupList = new ArrayList<>();

        for (Node node : user.getDistinctNodes()) {
            String groupName = node.getKey().substring(6);
            groupList.add(groupName);
        }

        groupList.sort(Comparator.comparing(this::getGroupWeight));
        Collections.reverse(groupList);

        return groupList.toArray(new String[]{});
    }
}
