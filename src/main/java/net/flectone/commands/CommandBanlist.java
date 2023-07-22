package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandBanlist extends FTabCompleter {

    public CommandBanlist(){
        super.commandName = "banlist";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if(fCommand.isHaveCD()) return true;

        int perpage = 4;

        int lastPage = (int) Math.ceil((double) FPlayerManager.getBannedPlayers().size() / perpage);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;

        page = Math.min(lastPage, page);

        ComponentBuilder componentBuilder = new ComponentBuilder("\n");

        String rawPlayerBanLine = Main.locale.getFormatString("command.banlist.player-ban", commandSender);
        String rawPlayerBanPermanentlyLine = Main.locale.getFormatString("command.banlist.player-ban-permanently", commandSender);

        String unbanButton = Main.locale.getFormatString("command.banlist.unban-button", commandSender);
        String unbanButtonHover = Main.locale.getFormatString("command.banlist.unban-hover", commandSender);

        if(FPlayerManager.getBannedPlayers().size() == 0){
            fCommand.sendMeMessage("command.banlist.no-banned-players");
            return true;
        }

        FPlayerManager.getBannedPlayers().stream().skip((long) (page - 1) * perpage).limit(perpage).forEach(fPlayer -> {
            String lastColor = ChatColor.getLastColors(componentBuilder.getCurrentComponent().toString());

            String banLine = fPlayer.isPermanentlyBanned() ? rawPlayerBanPermanentlyLine : rawPlayerBanLine;

            for (String part : splitLine(banLine, new String[]{"<unban>", "<name>", "<reason>", "<left>"})) {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(lastColor + part));
                lastColor = ChatColor.getLastColors((component.toString()));

                switch (part) {
                    case "<unban>":
                        component = new TextComponent(TextComponent.fromLegacyText(lastColor + unbanButton));
                        component.setClickEvent(
                                new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/unban " + fPlayer.getRealName()
                                )
                        );
                        component.setHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        TextComponent.fromLegacyText(
                                                unbanButtonHover.replaceAll(
                                                        "<player>",
                                                        fPlayer.getRealName()
                                                )
                                        )
                                )
                        );
                        break;
                    case "<name>":
                        component = new TextComponent(TextComponent.fromLegacyText(lastColor + fPlayer.getRealName()));
                        break;
                    case "<reason>":
                        component = new TextComponent(TextComponent.fromLegacyText(lastColor + fPlayer.getTempBanReason()));
                        break;
                    case "<left>":
                        String bannedTime = fPlayer.isPermanentlyBanned() ?
                                "∞" : ObjectUtil.convertTimeToString(fPlayer.getTempBanTime());
                        component = new TextComponent(TextComponent.fromLegacyText(lastColor + bannedTime));
                        break;
                }

                componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
            }
            componentBuilder.append(lastColor + "\n");
        });

        componentBuilder.append("\n");

        String pageType = "";
        if (page == lastPage) pageType = "last-";
        if (page == 1) pageType = "first-";
        if (page == lastPage && page == 1) pageType = "single-";

        String rawPageLine = Main.locale.getFormatString("command.banlist." + pageType + "pageline", commandSender);

        String nextPageButton = Main.locale.getFormatString("command.banlist.next-page", commandSender);
        String prevPageButton = Main.locale.getFormatString("command.banlist.prev-page", commandSender);

        for (String part : splitLine(rawPageLine, new String[]{"<last-page>", "<page>", "<next-page>", "<prev-page>"})) {
            String lastColor = ChatColor.getLastColors(componentBuilder.getCurrentComponent().toString());
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(lastColor + part));

            lastColor = ChatColor.getLastColors((component.toString()));

            switch (part) {
                case "<page>":
                    component = new TextComponent(TextComponent.fromLegacyText(lastColor + page));
                    break;
                case "<last-page>":
                    component = new TextComponent(TextComponent.fromLegacyText(lastColor + lastPage));
                    break;
                case "<next-page>":
                    component = new TextComponent(TextComponent.fromLegacyText(lastColor + nextPageButton));
                    component.setClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/" + s + " " + (page + 1)
                            )
                    );
                    break;
                case "<prev-page>":
                    component = new TextComponent(TextComponent.fromLegacyText(lastColor + prevPageButton));
                    component.setClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/" + s + " " + (page - 1)
                            )
                    );
                    break;
            }

            componentBuilder.append(component, ComponentBuilder.FormatRetention.NONE);
        }

        commandSender.spigot().sendMessage(componentBuilder.create());
        return true;
    }

    private ArrayList<String> splitLine(String line, String[] placeholders) {
        ArrayList<String> split = new ArrayList<>(List.of(line));

        for (String placeholder : placeholders) {
            split = (ArrayList<String>) split.stream().flatMap(part -> {
                        String[] sp = part.split("((?=@)|(?<=@))".replaceAll("@", placeholder));
                        return Arrays.stream(sp);
                    }).collect(Collectors.toList());
        }

        return split;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return wordsList;
    }
}
