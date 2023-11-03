package net.flectone.chat.module.commands;

import net.flectone.chat.model.mark.Mark;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.flectone.chat.manager.FileManager.commands;

public class CommandMark extends FCommand {
    public CommandMark(FModule module, String name) {
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
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        String color = args.length != 0 ? args[0].toUpperCase() : "WHITE";
        int range = commands.getInt(getName() + ".range");

        Mark.getMark(cmdSettings.getSender(), range, color).spawn();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();

        if (args.length == 1) {
            Mark.COLOR_VALUES.forEach(string -> isStartsWith(args[0], string));
        }

        return getSortedTabComplete();
    }
}
