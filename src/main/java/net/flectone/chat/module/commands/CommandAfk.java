package net.flectone.chat.module.commands;

import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.afkTimeout.AfkTimeoutModule;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandAfk extends FCommand {

    public CommandAfk(FModule fModule, String name) {
        super(fModule, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        CmdSettings commandCmdSettings = processCommand(commandSender, command);
        if (commandCmdSettings.isConsole()) {
            sendMessage(commandSender, getModule() + ".console");
            return true;
        }

        if (commandCmdSettings.isHaveCooldown()) {
            commandCmdSettings.getFPlayer().sendCDMessage(s);
            return true;
        }

        FPlayer fPlayer = commandCmdSettings.getFPlayer();
        if (fPlayer == null) return true;
        Player player = fPlayer.getPlayer();

        boolean isAfk = !fPlayer.isAfk();

        FModule fModule = moduleManager.get(AfkTimeoutModule.class);
        if (fModule instanceof AfkTimeoutModule afkTimeoutModule) {
            afkTimeoutModule.setAfk(player, isAfk, "/afk");
            return true;
        }

        String afkSuffix = isAfk
                ? locale.getVaultString(player, "commands.afk.suffix")
                : "";

        fPlayer.setAfkSuffix(MessageUtil.formatAll(player, afkSuffix));

        String afkMessage = locale.getVaultString(player, "commands.afk." + isAfk + "-message");
        player.sendMessage(MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, afkMessage)));

        fPlayer.playSound(player, player, this.toString());
        fPlayer.updateTeam();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return tabCompleteClear();
    }
}
