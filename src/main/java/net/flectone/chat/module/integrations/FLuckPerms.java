package net.flectone.chat.module.integrations;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class FLuckPerms implements FIntegration {

    public FLuckPerms() {
        init();
    }

    @Getter
    private static LuckPerms provider;


    @Override
    public void init() {
        RegisteredServiceProvider<LuckPerms> serviceProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        if (serviceProvider == null) return;

        provider = serviceProvider.getProvider();

        FlectoneChat.info("LuckPerms detected and hooked");
    }

    public int getPrimaryGroupWeight(@NotNull Player player) {
        String primaryGroup = getPrimaryGroup(player);
        if (primaryGroup == null) return 0;

        return getGroupWeight(primaryGroup);
    }

    public int getGroupWeight(@NotNull String groupName) {
        Group group = provider.getGroupManager().getGroup(groupName);
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

    @Nullable
    public ArrayList<String> getGroups(@NotNull Player player) {
        User user = provider.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;

        return new ArrayList<>(user.getInheritedGroups(user.getQueryOptions())
                .stream()
                .map(Group::getName)
                .toList());
    }
}
