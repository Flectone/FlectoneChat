package net.flectone.chat.module.serverMessage.greeting;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class GreetingListener extends FListener {

    public GreetingListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void playerJoinEvent(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;

        Bukkit.getScheduler().runTaskAsynchronously(FlectoneChat.getPlugin(), () ->
                ((GreetingModule) getModule()).send(player));
    }
}
