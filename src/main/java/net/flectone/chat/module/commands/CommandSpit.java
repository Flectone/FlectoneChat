package net.flectone.chat.module.commands;

import net.flectone.chat.model.spit.Spit;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSpit extends FCommand {
    public CommandSpit(FModule module, String name) {
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

        if (cmdSettings.isMuted()) {
            cmdSettings.getFPlayer().sendMutedMessage(command.getName());
            return true;
        }

        if (cmdSettings.isHaveCooldown()) {
            cmdSettings.getFPlayer().sendCDMessage(alias, command.getName());
            return true;
        }

        new Spit(cmdSettings.getSender(), this.toString()).spawn();

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return tabCompleteClear();
    }
}
