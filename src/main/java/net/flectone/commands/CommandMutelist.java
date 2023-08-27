package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.components.FListComponent;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandMutelist implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        int countMutes = Main.getDatabase().getCountRow("mutes");

        int perPage = config.getInt("command.mutelist.per-page");

        int lastPage = (int) Math.ceil((double) countMutes / perPage);

        if (strings.length != 0 &&
                (!StringUtils.isNumeric(strings[0])
                        || Integer.parseInt(strings[0]) < 1
                        || Integer.parseInt(strings[0]) > lastPage)) {

            fCommand.sendMeMessage("command.mutelist.page-not-exist");
            return;
        }

        if (countMutes == 0) {
            fCommand.sendMeMessage("command.mutelist.empty");
            return;
        }

        if (fCommand.isHaveCD()) return;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getFormatString("command.mutelist.title", commandSender)
                .replace("<count>", String.valueOf(countMutes));

        componentBuilder.append(FComponent.fromLegacyText(title)).append("\n\n");

        String unmuteButton = locale.getFormatString("command.mutelist.unmute-button", commandSender);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;

        page = Math.min(lastPage, page);

        Main.getDatabase().getModInfoList("mutes", perPage, (page - 1) * perPage).forEach(fPlayer -> {

            String playerMuteFormat = locale.getFormatString("command.mutelist.player-mute", commandSender)
                    .replace("<unmute>", unmuteButton)
                    .replace("<player>", fPlayer.getPlayerName())
                    .replace("<reason>", fPlayer.getReason())
                    .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getDifferenceTime()))
                    .replace("<moderator>", fPlayer.getModeratorName());

            String unbanHover = locale.getFormatString("command.mutelist.unmute-hover", commandSender)
                    .replace("<player>", fPlayer.getPlayerName());

            FComponent textComponent = new FComponent(playerMuteFormat)
                    .addHoverText(unbanHover)
                    .addRunCommand("/unmute " + fPlayer.getPlayerName());

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append(new FListComponent("mutelist", commandSender, page, lastPage).get());

        commandSender.spigot().sendMessage(componentBuilder.create());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            int perPage = config.getInt("command.mutelist.per-page");

            int lastPage = (int) Math.ceil((double) Main.getDatabase().getCountRow("mutes") / perPage);

            isDigitInArray(strings[0], "", 1, lastPage + 1);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "mutelist";
    }
}
