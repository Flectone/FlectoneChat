package net.flectone.commands;

import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.managers.HookManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandKick implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1) || fCommand.isHaveCD()) return true;

        if (fCommand.isDisabled()) {
            fCommand.sendMeMessage("command.you-disabled");
            return true;
        }

        String reason = strings.length > 1
                ? ObjectUtil.toString(strings, 1)
                : locale.getString("command.kick.default-reason");

        boolean announceModeration = config.getBoolean("command.kick.announce");

        if (strings[0].equals("@a")) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    kickPlayer(player, reason, fCommand, commandSender, announceModeration));
            return true;
        }

        Player playerToKick = Bukkit.getPlayer(strings[0]);
        if (playerToKick == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        kickPlayer(playerToKick, reason, fCommand, commandSender, announceModeration);
        return true;
    }

    private void kickPlayer(Player playerToKick, String reason, FCommand fCommand, CommandSender commandSender, boolean announceModeration) {
        String globalMessage = locale.getString("command.kick.global-message")
                .replace("<player>", playerToKick.getName())
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        if (announceModeration && HookManager.enabledDiscordSRV) FDiscordSRV.sendDiscordMessageToChannel(globalMessage);

        Set<Player> receivers = announceModeration
                ? new HashSet<>(Bukkit.getOnlinePlayers())
                : Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> player.hasPermission("flectonechat.kick") || player.equals(playerToKick))
                .collect(Collectors.toSet());

        fCommand.sendFilterGlobalMessage(receivers, globalMessage, "", null, false);

        String localMessage = locale.getFormatString("command.kick.local-message", playerToKick)
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        playerToKick.kickPlayer(localMessage);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> {
                isOnlinePlayer(strings[0]);
                isStartsWith(strings[0], "@a");
            }
            case 2 -> isTabCompleteMessage(strings[1]);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "kick";
    }
}
