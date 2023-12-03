package net.flectone.chat.module.commands;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CommandSpy extends FCommand {

    public CommandSpy(FModule module, String name) {
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

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendErrorMessage(commandSender, getModule() + ".console");
            return true;
        }

        String value = cmdSettings.getFPlayer().getSettings().getValue(Settings.Type.SPY);
        boolean isEnabled = value != null && value.equals("1");

        cmdSettings.getFPlayer().getSettings().set(Settings.Type.SPY, isEnabled ? "-1" : "1");

        database.execute(() -> database.updateFPlayer("spy", cmdSettings.getFPlayer()));

        String message = locale.getVaultString(commandSender, this + "." + !isEnabled + "-message");
        commandSender.sendMessage(MessageUtil.formatAll(cmdSettings.getSender(), message));

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return tabCompleteClear();
    }

    public static void send(@NotNull Player sender, @NotNull String action, @NotNull Collection<Player> recipients,
                            @NotNull Type type, @NotNull String message) {

        FlectoneChat plugin = FlectoneChat.getPlugin();

        FConfiguration commands = plugin.getFileManager().getCommands();
        if (!commands.getBoolean("spy.enable")) return;

        if (!commands.getBoolean("spy.message-type." + type)) return;

        List<String> commandSpyList = commands.getStringList("spy.list");
        if (!commandSpyList.contains(action)) return;

        FConfiguration locale = plugin.getFileManager().getLocale();

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("flectonechat.commands.spy"))
                .filter(player -> {
                    FPlayer fPlayer = plugin.getPlayerManager().get(player);
                    if (fPlayer == null) return false;

                    String value = fPlayer.getSettings().getValue(Settings.Type.SPY);
                    return value != null && value.equals("1");
                })
                .filter(player -> !player.equals(sender))
                .filter(player -> !recipients.contains(player))
                .forEach(player -> {
                    String configString = locale.getVaultString(player, "commands.spy." + type  + "-message")
                            .replace("<action>", action)
                            .replace("<message>", message);

                    configString = MessageUtil.formatPlayerString(sender, configString);
                    player.sendMessage(MessageUtil.formatAll(sender, player, configString));
                });
    }

    public enum Type {
        DEFAULT("default"),
        USAGE("usage"),
        ERROR("error");

        private final String name;

        Type(@NotNull String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
