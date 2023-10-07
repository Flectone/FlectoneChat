package net.flectone.commands;

import net.flectone.Main;
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

public class CommandWarn implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));

        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return;

        String stringTime = strings[1];

        if (!fCommand.isStringTime(stringTime) || !StringUtils.isNumeric(stringTime.substring(0, stringTime.length() - 1))) {
            fCommand.sendUsageMessage();
            return;
        }

        String playerName = strings[0];
        FPlayer warnedFPlayer = FPlayerManager.getPlayerFromName(playerName);

        if (warnedFPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        int time = fCommand.getTimeFromString(stringTime);

        if (time < -1) {
            fCommand.sendMeMessage("command.long-number");
            return;
        }

        if (fCommand.isHaveCD()) return;

        if (warnedFPlayer.getWarnList() == null) warnedFPlayer.synchronizeDatabase();

        String reason = strings.length > 2
                ? ObjectUtil.toString(strings, 2)
                : locale.getString("command.warn.default-reason");

        String formatString = locale.getString("command.warn.global-message")
                .replace("<player>", warnedFPlayer.getRealName())
                .replace("<time>", ObjectUtil.convertTimeToString(time))
                .replace("<count>", String.valueOf(warnedFPlayer.getRealWarnsCount() + 1))
                .replace("<reason>", reason)
                .replace("<moderator>", commandSender.getName());

        boolean announceModeration = config.getBoolean("command.warn.announce");

        if (announceModeration && HookManager.enabledDiscordSRV) FDiscordSRV.sendDiscordMessageToChannel(formatString);

        Set<Player> receivers = announceModeration
                ? new HashSet<>(Bukkit.getOnlinePlayers())
                : Bukkit.getOnlinePlayers().parallelStream()
                .filter(player -> player.hasPermission("flectonechat.warn") || player.equals(warnedFPlayer.getPlayer()))
                .collect(Collectors.toSet());

        fCommand.sendFilterGlobalMessage(receivers, formatString, "", null, false);

        String moderator = (commandSender instanceof Player player)
                ? player.getUniqueId().toString()
                : null;

        warnedFPlayer.warn(time, reason, moderator);
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
        return "warn";
    }
}
