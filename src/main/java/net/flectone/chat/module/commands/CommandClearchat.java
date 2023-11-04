package net.flectone.chat.module.commands;

import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandClearchat extends FCommand {

    private static final String OTHER_PERMISSION = "flectonechat.commands.clearchat.other";
    private static final String CLEARED_STRING = " \n".repeat(100);

    public CommandClearchat(FModule module, String name) {
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
        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias);
            return true;
        }

        if (commandSender.hasPermission(OTHER_PERMISSION) && args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    player.sendMessage(CLEARED_STRING));
        }

        commandSender.sendMessage(CLEARED_STRING);
        sendMessage(commandSender, this + ".message");

        if (!cmdSettings.isConsole()) {
            cmdSettings.getFPlayer().playSound(cmdSettings.getSender(), cmdSettings.getSender(), this.toString());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        tabCompleteClear();

        if (!commandSender.hasPermission(OTHER_PERMISSION)) {
            return getTAB_COMPLETE();
        }

        if (args.length == 1) {
            isStartsWith(args[0], "all");
        }

        return getTAB_COMPLETE();
    }
}
