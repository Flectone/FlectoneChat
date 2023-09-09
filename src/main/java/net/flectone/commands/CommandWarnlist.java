package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.components.FListComponent;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandWarnlist implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(1)) return;

        FPlayer fPlayer = FPlayerManager.getPlayerFromName(strings[0]);

        if (fPlayer == null) {
            fCommand.sendMeMessage("command.null-player");
            return;
        }

        if (fPlayer.getWarnList() == null) fPlayer.synchronizeDatabase();

        int countWarns = fPlayer.getWarnList().size();

        int perPage = config.getInt("command.warnlist.per-page");

        int lastPage = (int) Math.ceil((double) countWarns / perPage);

        if (strings.length != 1 &&
                (!StringUtils.isNumeric(strings[1])
                        || Integer.parseInt(strings[1]) < 1
                        || Integer.parseInt(strings[1]) > lastPage)) {

            fCommand.sendMeMessage("command.warnlist.page-not-exist");
            return;
        }

        if (countWarns == 0) {
            fCommand.sendMeMessage("command.warnlist.empty");
            return;
        }

        if (fCommand.isHaveCD()) return;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getFormatString("command.warnlist.title", commandSender)
                .replace("<count>", String.valueOf(countWarns));

        componentBuilder.append(FComponent.fromLegacyText(title)).append("\n\n");

        String unwarnButton = locale.getFormatString("command.warnlist.unwarn-button", commandSender);

        int page = strings.length > 1 ? Math.max(1, Integer.parseInt(strings[1])) : 1;

        page = Math.min(lastPage, page);

        AtomicInteger atomicInteger = new AtomicInteger((page - 1) * perPage + 1);

        fPlayer.getWarnList().stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .forEach(playerWarn -> {

            String warnFormat = locale.getFormatString("command.warnlist.player-warn", commandSender)
                    .replace("<unwarn>", unwarnButton)
                    .replace("<player>", playerWarn.getPlayerName())
                    .replace("<reason>", playerWarn.getReason())
                    .replace("<time>", ObjectUtil.convertTimeToString(playerWarn.getDifferenceTime()))
                    .replace("<moderator>", playerWarn.getModeratorName());

            String unbanHover = locale.getFormatString("command.warnlist.unwarn-hover", commandSender)
                    .replace("<player>", playerWarn.getPlayerName());

            FComponent textComponent = new FComponent(warnFormat)
                    .addHoverText(unbanHover)
                    .addRunCommand("/unwarn " + playerWarn.getPlayerName() + " " + atomicInteger.getAndIncrement());

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append(new FListComponent("warnlist " + fPlayer.getRealName() + " ", "warnlist", commandSender, page, lastPage).get());

        commandSender.spigot().sendMessage(componentBuilder.create());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();


        switch (strings.length) {
            case 1 -> Main.getDatabase().getPlayerNameList("warns", "player")
                    .parallelStream()
                    .forEach(playerName -> isStartsWith(strings[0], playerName));
            case 2 -> {
                String playerName = strings[0];
                FPlayer fPlayer = FPlayerManager.getPlayerFromName(playerName);

                if (fPlayer == null) break;

                if (!fPlayer.isOnline() && fPlayer.getWarnList() == null)
                    fPlayer.synchronizeDatabase();

                if (fPlayer.getWarnList().isEmpty()) break;

                int perPage = config.getInt("command.warnlist.per-page");

                int lastPage = (int) Math.ceil((double) fPlayer.getWarnList().size() / perPage);
                isDigitInArray(strings[1], "", 1, lastPage + 1);
            }
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "warnlist";
    }
}
