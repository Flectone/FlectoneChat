package net.flectone.misc.components;

import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FListComponent extends FComponent{

    public FListComponent(@NotNull String command, @NotNull String commandName, @NotNull CommandSender commandSender, int page, int lastPage) {
        String pageLine = locale.getFormatString("command." + commandName + ".page-line", commandSender)
                .replace("<page>", String.valueOf(page))
                .replace("<last-page>", String.valueOf(lastPage));

        String chatColor = "";
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (String part : ObjectUtil.splitLine(pageLine, new ArrayList<>(List.of("<prev-page>", "<next-page>")))) {

            int pageNumber = page;
            String button = null;

            switch (part) {
                case "<prev-page>" -> {
                    pageNumber--;
                    button = locale.getFormatString("command." + commandName + ".prev-page", commandSender);
                }
                case "<next-page>" -> {
                    pageNumber++;
                    button = locale.getFormatString("command." + commandName + ".next-page", commandSender);
                }
            }

            FComponent component;
            if (button != null) {

                component = new FComponent(chatColor + button)
                        .addRunCommand("/" + command + " " + pageNumber);

            } else component = new FComponent(chatColor + part);

            componentBuilder.append(component.get(), ComponentBuilder.FormatRetention.NONE);

            chatColor = getLastColor(chatColor, componentBuilder);
        }

        set(componentBuilder.create());
    }

    public FListComponent(String commandName, CommandSender commandSender, int page, int lastPage) {
        this(commandName, commandName, commandSender, page, lastPage);
    }
}
