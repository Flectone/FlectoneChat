package net.flectone.commands;

import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.managers.FPlayerManager;
import net.flectone.managers.HookManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.apache.commons.lang.StringUtils;
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

public class CommandTempban implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return true;

        String stringTime = strings.length > 1 ? strings[1] : "permanent";

        if ((!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1)))
                && !stringTime.equals("permanent") && !stringTime.equals("0")) {
            fCommand.sendUsageMessage();
            return true;
        }

        String playerName = strings[0];
        FPlayer bannedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (bannedFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        int time = fCommand.getTimeFromString(stringTime);

        if (time < -1) {
            fCommand.sendMeMessage("command.long-number");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        String reason = strings.length > 2 ? ObjectUtil.toString(strings, 2) : locale.getString("command.tempban.default-reason");

        String globalStringMessage = time == -1 ? "command.ban.global-message" : "command.tempban.global-message";

        String globalMessage = locale.getString(globalStringMessage)
                .replace("<player>", bannedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        boolean announceModeration = config.getBoolean("command.tempban.announce");

        if (announceModeration && HookManager.enabledDiscordSRV) FDiscordSRV.sendDiscordMessageToChannel(globalMessage);

        Set<Player> receivers = announceModeration
                ? new HashSet<>(Bukkit.getOnlinePlayers())
                : Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> player.hasPermission("flectonechat.ban") || player.equals(bannedFPlayer.getPlayer()))
                .collect(Collectors.toSet());

        fCommand.sendFilterGlobalMessage(receivers, globalMessage, "", null, false);

        String moderator = (commandSender instanceof Player player)
                ? player.getUniqueId().toString()
                : null;

        bannedFPlayer.tempban(time, reason, moderator);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> isConfigOnlineModePlayer(strings[0]);
            case 2 -> {
                isFormatString(strings[1]);
                isStartsWith(strings[1], "permanent");
                isStartsWith(strings[1], "0");
            }
            case 3 -> isTabCompleteMessage(strings[2], "tab-complete.reason");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "tempban";
    }
}
