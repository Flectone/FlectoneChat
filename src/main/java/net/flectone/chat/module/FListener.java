package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class FListener implements Listener, FAction {

    private final FModule module;

    public FListener(FModule module) {
        this.module = module;
    }

    public boolean hasNoPermission(@NotNull Player player) {
        return !player.hasPermission(getPermission());
    }

    public boolean hasNoPermission(@NotNull Player player, @NotNull String string) {
        return !player.hasPermission(getPermission() + "." + string);
    }

    public String getPermission() {
        return "flectonechat." + getModule();
    }

    public void register() {
        Bukkit.getServer().getPluginManager().registerEvents(this, FlectoneChat.getInstance());
    }
}
