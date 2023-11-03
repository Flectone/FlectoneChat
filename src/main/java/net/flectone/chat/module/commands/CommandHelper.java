package net.flectone.chat.module.commands;

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

import static net.flectone.chat.manager.FileManager.locale;

public class CommandHelper extends FCommand {

    private static final String OTHER_PERMISSION = "flectonechat.commands.helper.other";

    public CommandHelper(FModule module, String name) {
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

        if (args.length == 0) {
            sendUsageMessage(commandSender, alias);
            return true;
        }

        CmdSettings cmdSettings = new CmdSettings(commandSender, command);

        if (cmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        Collection<Player> playerList = (Collection<Player>) Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission(OTHER_PERMISSION))
                .toList();

        if (playerList.isEmpty()) {
            sendMessage(commandSender, this + ".no-helpers");
            return true;
        }

        String serverMessage = locale.getVaultString(commandSender, this + ".server-message");
        String message = MessageUtil.joinArray(args, 0, " ");

        sendGlobalMessage(playerList, cmdSettings.getSender(), cmdSettings.getItemStack(), serverMessage,
                message, true);

        sendMessage(commandSender, this + ".player-message");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        tabCompleteClear();
        if (args.length == 1) {
            isTabCompleteMessage(commandSender, args[0], "message");
        }

        return getSortedTabComplete();
    }
}
