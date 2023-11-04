package net.flectone.chat.module.commands;

import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.commands;
import static net.flectone.chat.manager.FileManager.locale;

public class CommandMaintenance extends FCommand {
    public CommandMaintenance(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;

        FActionManager.add(new MaintenanceListener(null));
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        String arg = args[0].toLowerCase();

        if (!arg.equals("off") && !arg.equals("on")) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        boolean isEnabled = commands.getBoolean(getName() + ".turned-on");

        if (arg.equals("on") && isEnabled) {
            sendMessage(commandSender, this + ".already");
            return true;
        }

        if (arg.equals("off") && !isEnabled) {
            sendMessage(commandSender, this + ".not");
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        if (cmdSettings.isMuted()) {
            cmdSettings.getFPlayer().sendMutedMessage();
            return true;
        }

        isEnabled = arg.equals("on");

        if (isEnabled) {

            String kickedMessage = locale.getVaultString(commandSender, this + ".kicked-message");

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !player.hasPermission("flectonechat.commands.maintenance"))
                    .forEach(player -> player.kickPlayer(MessageUtil.formatAll(player, kickedMessage)));
        }

        String serverMessage = locale.getVaultString(commandSender, this + ".turned-" + arg);
        sendGlobalMessage(cmdSettings.getSender(), cmdSettings.getItemStack(), serverMessage, "", false);

        commands.set(getName() + ".turned-on", isEnabled);
        commands.save();

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();
        if (args.length == 1) {
            isStartsWith(args[0], "on");
            isStartsWith(args[0], "off");
        }

        return getSortedTabComplete();
    }
}
