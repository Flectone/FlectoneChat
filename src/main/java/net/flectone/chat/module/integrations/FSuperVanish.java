package net.flectone.chat.module.integrations;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FModuleManager;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.serverMessage.join.JoinModule;
import net.flectone.chat.module.serverMessage.quit.QuitModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class FSuperVanish implements Listener, FIntegration {

    private FModuleManager moduleManager;
    private FConfiguration locale;

    public FSuperVanish() {
        init();
    }

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, FlectoneChat.getPlugin());
        FlectoneChat.info("SuperVanish detected and hooked");

        FlectoneChat plugin = FlectoneChat.getPlugin();
        moduleManager = plugin.getModuleManager();
        locale = plugin.getFileManager().getLocale();
    }

    @EventHandler
    public void onHide(@NotNull PlayerHideEvent event) {
        if (event.isCancelled()) return;

        FModule fModule = moduleManager.get(QuitModule.class);
        if (fModule instanceof QuitModule quitModule) {
            Player player = event.getPlayer();
            String string = locale.getVaultString(player, "server-message.quit.message");
            quitModule.sendAll(player, string);
            event.setSilent(true);
        }
    }

    @EventHandler
    public void onShow(@NotNull PlayerShowEvent event) {
        if (event.isCancelled()) return;

        FModule fModule = moduleManager.get(JoinModule.class);
        if (fModule instanceof JoinModule joinModule) {
            Player player = event.getPlayer();
            String string = locale.getVaultString(player, "server-message.join.message");
            joinModule.sendAll(player, string);
            event.setSilent(true);
        }
    }
}
