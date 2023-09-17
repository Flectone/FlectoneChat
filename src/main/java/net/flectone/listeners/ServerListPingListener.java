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

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void updateServerList(@NotNull ServerListPingEvent event) {
        if (config.getBoolean("command.maintenance.turn-on")) {
            String motd = locale.getFormatString("server.motd.maintenance", null);
            event.setMotd(motd);
            setIcon(event, "maintenance");
            event.setMaxPlayers(-1);
            return;
        }

        if (config.getBoolean("server.motd.messages.enable")) {
            List<String> motds = locale.getStringList("server.motd.messages");
            if (!motds.isEmpty()) {
                int numberMotd = ObjectUtil.nextInt(0, motds.size());
                event.setMotd(ObjectUtil.formatString(motds.get(numberMotd), null));
            }
        }

        if (config.getBoolean("server.online.count.enable")) {
            event.setMaxPlayers(config.getInt("server.online.count.digit"));
        }

        if (config.getBoolean("server.icon.enable")) {
            List<String> iconNames = config.getStringList("server.icon.names");
            if (!iconNames.isEmpty()) {
                int numberIcon = config.getString("server.icon.mode").equals("single")
                        ? 0
                        : ObjectUtil.nextInt(0, iconNames.size());

                setIcon(event, iconNames.get(numberIcon));
            }
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
