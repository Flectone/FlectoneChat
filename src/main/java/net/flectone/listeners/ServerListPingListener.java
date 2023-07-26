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

        if(Main.config.getBoolean("server.icon.enable")){
            List<String> iconNames = Main.config.getStringList("server.icon.names");

            int numberIcon = Main.config.getString("server.icon.mode").equals("single") ? 0 :
                    new Random().nextInt(0, iconNames.size());

            setIcon(event, iconNames.get(numberIcon));
        }
    }

    private void setIcon(ServerListPingEvent event, String iconName){
        try {
            CachedServerIcon serverIcon = Bukkit.loadServerIcon(new File(Main.getInstance().getDataFolder(), "icons" + File.separator + iconName + ".png"));
            event.setServerIcon(serverIcon);
        } catch (Exception exception){
            Main.warning("Unable to load and install " + iconName + ".png image");
            exception.printStackTrace();
        }
    }

}
