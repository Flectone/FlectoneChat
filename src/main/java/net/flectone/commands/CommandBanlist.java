package net.flectone.commands;

import net.flectone.Main;
import net.flectone.misc.commands.FCommands;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CommandBanlist extends FTabCompleter {

    public CommandBanlist() {
        super.commandName = "banlist";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        Set<FPlayer> bannedPlayers = FPlayerManager.getBannedPlayers();

        int perPage = Main.config.getInt("command.banlist.per-page");

        int lastPage = (int) Math.ceil((double) bannedPlayers.size() / perPage);

        if (strings.length != 0 &&
                (!StringUtils.isNumeric(strings[0])
                        || Integer.parseInt(strings[0]) < 1
                        || Integer.parseInt(strings[0]) > lastPage)) {

            fCommand.sendMeMessage("command.banlist.page-not-exist");
            return true;
        }

        if (bannedPlayers.isEmpty()) {
            fCommand.sendMeMessage("command.banlist.empty");
            return true;
        }

        if (fCommand.isHaveCD()) return true;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = Main.locale.getFormatString("command.banlist.title", commandSender)
                .replace("<count>", String.valueOf(bannedPlayers.size()));

        componentBuilder.append(TextComponent.fromLegacyText(title)).append("\n\n");

        String unbanButton = Main.locale.getFormatString("command.banlist.unban-button", commandSender);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;

        page = Math.min(lastPage, page);

        bannedPlayers.stream().skip((long) (page - 1) * perPage).limit(perPage).forEach(fPlayer -> {

            String playerBanFormat = "command.banlist.player-ban";
            if (fPlayer.isPermanentlyBanned()) playerBanFormat += "-permanently";

            playerBanFormat = Main.locale.getFormatString(playerBanFormat, commandSender)
                    .replace("<unban>", unbanButton)
                    .replace("<player>", fPlayer.getRealName())
                    .replace("<reason>", fPlayer.getBanReason())
                    .replace("<time>", ObjectUtil.convertTimeToString(fPlayer.getTempBanTime()));

            String unbanHover = Main.locale.getFormatString("command.banlist.unban-hover", commandSender)
                    .replace("<player>", fPlayer.getRealName());

            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(playerBanFormat));
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(unbanHover)));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unban " + fPlayer.getRealName()));

            componentBuilder.append(textComponent).append("\n\n");
        });

        String pageLine = Main.locale.getFormatString("command.banlist.page-line", commandSender)
                .replace("<page>", String.valueOf(page))
                .replace("<last-page>", String.valueOf(lastPage));

        String chatColor = "";

        for (String part : ObjectUtil.splitLine(pageLine, new ArrayList<>(List.of("<prev-page>", "<next-page>")))) {

            int pageNumber = page;
            String button = null;

            switch (part) {
                case "<prev-page>":
                    pageNumber--;
                    button = Main.locale.getFormatString("command.banlist.prev-page", commandSender);
                    break;
                case "<next-page>":
                    pageNumber++;
                    button = Main.locale.getFormatString("command.banlist.next-page", commandSender);
                    break;
            }

            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + part));
            if (button != null) {
                textComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + button));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banlist " + pageNumber));
            }

            componentBuilder.append(textComponent, ComponentBuilder.FormatRetention.NONE);

            chatColor = ChatColor.getLastColors(chatColor + componentBuilder.getCurrentComponent().toString());
        }

        commandSender.spigot().sendMessage(componentBuilder.create());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            int perPage = Main.config.getInt("command.banlist.per-page");

            int lastPage = (int) Math.ceil((double) FPlayerManager.getBannedPlayers().size() / perPage);

            for (int x = 0; x < lastPage; x++) {
                isStartsWith(strings[0], String.valueOf(x + 1));
            }

        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
