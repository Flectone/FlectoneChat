package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CommandGeolocate extends FCommand {

    private static final String HTTP_URL = "http://ip-api.com/line/<ip>?fields=17031449";

    public CommandGeolocate(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        Bukkit.getScheduler().runTaskAsynchronously(FlectoneChat.getPlugin(), () ->
                asyncOnCommand(commandSender, command, alias, args));

        return true;
    }

    public void asyncOnCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                               @NotNull String[] args) {

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null || IntegrationsModule.isVanished(player)) {
            sendErrorMessage(commandSender, getModule() + ".null-player");
            return;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return;
        }

        if (player.getAddress() == null || player.getAddress().getHostName() == null) {
            sendErrorMessage(commandSender, this + ".fail");
            return;
        }

        String playerIP = player.getAddress().getHostName();
        List<String> request = getHttp(HTTP_URL.replace("<ip>", playerIP));
        if (request.isEmpty() || request.get(0).equals("fail")) {
            sendErrorMessage(commandSender, this + ".fail");
            return;
        }

        String message = locale.getVaultString(commandSender, this + ".message")
                .replace("<country>", request.get(1))
                .replace("<region_name>", request.get(2))
                .replace("<city>", request.get(3))
                .replace("<timezone>", request.get(4))
                .replace("<mobile>", request.get(5))
                .replace("<proxy>", request.get(6))
                .replace("<hosting>", request.get(7))
                .replace("<query>", request.get(8));

        Player sender = commandSender instanceof Player playerSender ? playerSender : null;

        message = MessageUtil.formatPlayerString(player, message);
        message = MessageUtil.formatAll(sender, player, message);

        sendFormattedMessage(commandSender, message);

        if (sender == null) {
            FPlayer fPlayer = playerManager.get(player);
            if (fPlayer == null) return;
            fPlayer.playSound(player, player, this.toString());
        }

    }

    @NotNull
    public List<String> getHttp(String url) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));

            String line;
            while((line = br.readLine()) != null) {
                arrayList.add(line);
            }

            br.close();
        } catch (IOException ignored) {}
        return arrayList;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isOnlinePlayer(args[0]);
        }

        return getSortedTabComplete();
    }
}
