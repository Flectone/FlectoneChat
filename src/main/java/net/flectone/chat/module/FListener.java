package net.flectone.chat.module;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.file.FConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class FListener implements Listener, FAction {

    private final FModule module;
    protected final FlectoneChat plugin;
    protected final FConfiguration config;
    protected final FConfiguration locale;
    protected final FConfiguration commands;
    protected final FPlayerManager playerManager;

    public FListener(FModule module) {
        this.module = module;

        plugin = FlectoneChat.getPlugin();
        config = plugin.getFileManager().getConfig();
        locale = plugin.getFileManager().getLocale();
        commands = plugin.getFileManager().getCommands();
        playerManager = plugin.getPlayerManager();
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
        Bukkit.getServer().getPluginManager().registerEvents(this, FlectoneChat.getPlugin());
    }
}
