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

public class CommandBanlist implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        int bansCount = Main.getDatabase().getCountRow("bans");

        int perPage = config.getInt("command.banlist.per-page");

        int lastPage = (int) Math.ceil((double) bansCount / perPage);

        if (strings.length != 0 &&
                (!StringUtils.isNumeric(strings[0]) || Integer.parseInt(strings[0]) < 1 || Integer.parseInt(strings[0]) > lastPage)) {

            fCommand.sendMeMessage("command.banlist.page-not-exist");
            return;
        }

        if (bansCount == 0) {
            fCommand.sendMeMessage("command.banlist.empty");
            return;
        }

        if (fCommand.isHaveCD()) return;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getFormatString("command.banlist.title", commandSender)
                .replace("<count>", String.valueOf(bansCount));

        componentBuilder.append(FComponent.fromLegacyText(title)).append("\n\n");

        String unbanButton = locale.getFormatString("command.banlist.unban-button", commandSender);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;
        page = Math.min(lastPage, page);

        Main.getDatabase().getModInfoList("bans", perPage, (page - 1) * perPage).forEach(dPlayer -> {
            String playerBanFormat = "command.banlist.player-ban";
            if (dPlayer.getTime() == -1) playerBanFormat += "-permanently";

            playerBanFormat = locale.getFormatString(playerBanFormat, commandSender)
                    .replace("<unban>", unbanButton)
                    .replace("<player>", dPlayer.getPlayerName())
                    .replace("<reason>", dPlayer.getReason())
                    .replace("<time>", ObjectUtil.convertTimeToString(dPlayer.getDifferenceTime()))
                    .replace("<moderator>", dPlayer.getModeratorName());

            String unbanHover = locale.getFormatString("command.banlist.unban-hover", commandSender)
                    .replace("<player>", dPlayer.getPlayerName());

            FComponent textComponent = new FComponent(playerBanFormat)
                    .addHoverText(unbanHover)
                    .addRunCommand("/unban " + dPlayer.getPlayerName());

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append(new FListComponent("banlist", commandSender, page, lastPage).get());

        commandSender.spigot().sendMessage(componentBuilder.create());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            int perPage = config.getInt("command.banlist.per-page");

            int lastPage = (int) Math.ceil((double) Main.getDatabase().getCountRow("bans") / perPage);

            isDigitInArray(strings[0], "", 1, lastPage + 1);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "banlist";
    }
}
