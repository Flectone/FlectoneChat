package net.flectone.commands;

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
import java.util.Set;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandBanlist implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        Set<FPlayer> bannedPlayers = FPlayerManager.getBannedPlayers();

        int perPage = config.getInt("command.banlist.per-page");

        int lastPage = (int) Math.ceil((double) bannedPlayers.size() / perPage);

        if (strings.length != 0 &&
                (!StringUtils.isNumeric(strings[0]) || Integer.parseInt(strings[0]) < 1 || Integer.parseInt(strings[0]) > lastPage)) {

            fCommand.sendMeMessage("command.banlist.page-not-exist");
            return true;
        }

        if (bannedPlayers.isEmpty()) {
            fCommand.sendMeMessage("command.banlist.empty");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getFormatString("command.banlist.title", commandSender)
                .replace("<count>", String.valueOf(bannedPlayers.size()));

        componentBuilder.append(FComponent.fromLegacyText(title)).append("\n\n");

        String unbanButton = locale.getFormatString("command.banlist.unban-button", commandSender);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;
        page = Math.min(lastPage, page);

        bannedPlayers.stream().skip((long) (page - 1) * perPage).limit(perPage).forEach(fPlayer -> {
            String playerBanFormat = "command.banlist.player-ban";
            if (fPlayer.isPermanentlyBanned()) playerBanFormat += "-permanently";

            playerBanFormat = locale.getFormatString(playerBanFormat, commandSender)
                    .replace("<unban>", unbanButton)
                    .replace("<player>", fPlayer.getRealName())
                    .replace("<reason>", fPlayer.getBanReason())
                    .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getTempBanTime()));

            String unbanHover = locale.getFormatString("command.banlist.unban-hover", commandSender)
                    .replace("<player>", fPlayer.getRealName());

            FComponent textComponent = new FComponent(playerBanFormat)
                    .addHoverText(unbanHover)
                    .addRunCommand("/unban " + fPlayer.getRealName());

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append(new FListComponent("banlist", commandSender, page, lastPage).get());

        commandSender.spigot().sendMessage(componentBuilder.create());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            int perPage = config.getInt("command.banlist.per-page");

            int lastPage = (int) Math.ceil((double) FPlayerManager.getBannedPlayers().size() / perPage);

            for (int x = 0; x < lastPage; x++) {
                isStartsWith(strings[0], String.valueOf(x + 1));
            }

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
