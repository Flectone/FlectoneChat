package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Random;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void updateServerList(@NotNull ServerListPingEvent event) {
        if (Main.config.getBoolean("command.maintenance.enable")) {
            String motd = Main.locale.getFormatString("server.motd.maintenance", null);
            event.setMotd(motd);
            setIcon(event, "maintenance");
            event.setMaxPlayers(-1);
            return;
        }

        if (Main.config.getBoolean("server.motd.messages.enable")) {
            List<String> motds = Main.locale.getStringList("server.motd.messages");

            int numberMotd = new Random().nextInt(0, motds.size());

            event.setMotd(ObjectUtil.formatString(motds.get(numberMotd), null));
        }

        if (Main.config.getBoolean("server.online.count.enable")) {
            event.setMaxPlayers(Main.config.getInt("server.online.count.digit"));
        }

        if (Main.config.getBoolean("server.icon.enable")) {
            List<String> iconNames = Main.config.getStringList("server.icon.names");

            int numberIcon = Main.config.getString("server.icon.mode").equals("single") ? 0 :
                    new Random().nextInt(0, iconNames.size());

            setIcon(event, iconNames.get(numberIcon));
        }
    }

    private void setIcon(@NotNull ServerListPingEvent event, @NotNull String iconName) {
        try {
            CachedServerIcon serverIcon = Bukkit.loadServerIcon(new File(Main.getInstance().getDataFolder(), "icons" + File.separator + iconName + ".png"));
            event.setServerIcon(serverIcon);
        } catch (Exception exception) {
            Main.warning("Unable to load and install " + iconName + ".png image");
            exception.printStackTrace();
        }
    }

}
