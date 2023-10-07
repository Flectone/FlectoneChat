package net.flectone.commands;

import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.voicechats.plasmovoice.FPlasmoVoice;
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

import java.util.*;
import java.util.stream.Collectors;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandMute implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return true;

        String stringTime = strings[1];

        if (!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))) {
            fCommand.sendUsageMessage();
            return true;
        }

        String playerName = strings[0];
        FPlayer mutedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (mutedFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return true;
        }

        int time = fCommand.getTimeFromString(stringTime);

        if (time < -1) {
            fCommand.sendMeMessage("command.long-number");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        String reason = strings.length > 2
                ? ObjectUtil.toString(strings, 2)
                : locale.getString("command.mute.default-reason");

        String formatString = locale.getString("command.mute.global-message")
                .replace("<player>", mutedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        boolean announceModeration = config.getBoolean("command.mute.announce");

        if (announceModeration && HookManager.enabledDiscordSRV) FDiscordSRV.sendDiscordMessageToChannel(formatString);

        Set<Player> receivers = announceModeration
                ? new HashSet<>(Bukkit.getOnlinePlayers())
                : Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> player.hasPermission("flectonechat.mute") || player.equals(mutedFPlayer.getPlayer()))
                .collect(Collectors.toSet());

        fCommand.sendFilterGlobalMessage(receivers, formatString, "", null, false);

        String moderator = (commandSender instanceof Player player)
                ? player.getUniqueId().toString()
                : null;

        mutedFPlayer.mute(time, reason, moderator);

        if (HookManager.enabledPlasmoVoice) {
            FPlasmoVoice.mute(mutedFPlayer.isMuted(), mutedFPlayer.getRealName(), strings[1], reason);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length){
            case 1 -> isConfigOnlineModePlayer(strings[0]);
            case 2 -> isFormatString(strings[1]);
            case 3 -> isTabCompleteMessage(strings[2], "tab-complete.reason");
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "mute";
    }
}
