package net.flectone.listeners;

import net.flectone.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void updateServerList(ServerListPingEvent event){
        if(Main.config.getBoolean("server.motd.message.enable")){
            event.setMotd(Main.locale.getFormatString("server.motd.message", null));
        }

        if(Main.config.getBoolean("server.online.count.enable")){
            event.setMaxPlayers(Main.config.getInt("server.online.count.digit"));
        }
    }
}
