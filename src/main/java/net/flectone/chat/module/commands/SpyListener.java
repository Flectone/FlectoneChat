package net.flectone.chat.module.commands;

import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.flectone.chat.manager.FileManager.commands;
import static net.flectone.chat.manager.FileManager.locale;

public class SpyListener extends FListener {
    public SpyListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void commandExecuteEvent(@NotNull PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0].substring(1);
        send(event.getPlayer(), command, event.getMessage());
    }

    public static void send(@NotNull Player sender, @NotNull String action, @NotNull String message) {
        if (!commands.getBoolean("spy.enable")) return;

        List<String> commandSpyList = commands.getStringList("spy.list");
        if (!commandSpyList.contains(action)) return;

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("flectonechat.commands.spy"))
                .filter(player -> {
                    FPlayer fPlayer = FPlayerManager.get(player);
                    if (fPlayer == null) return false;

                    String value = fPlayer.getSettings().getValue(Settings.Type.SPY);
                    return value != null && value.equals("1");
                })
                .forEach(player -> {
                    String configString = locale.getVaultString(player, "commands.spy.message")
                            .replace("<action>", action)
                            .replace("<message>", message);

                    configString = MessageUtil.formatPlayerString(sender, configString);
                    player.sendMessage(MessageUtil.formatAll(sender, player, configString));
                });
    }
}