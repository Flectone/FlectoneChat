package net.flectone.commands;

import net.flectone.Main;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.components.FListComponent;
import net.flectone.misc.entity.FPlayer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class CommandIgnorelist implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Main.getDataThreadPool().execute(() -> command(commandSender, command, s, strings));
        return true;
    }

    private void command(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        FPlayer commandFPlayer = fCommand.getFPlayer();
        if (fCommand.isConsoleMessage() || commandFPlayer == null) return;

        int ignores = commandFPlayer.getIgnoreList().size();

        int perPage = config.getInt("command.ignorelist.per-page");

        int lastPage = (int) Math.ceil((double) ignores / perPage);

        if (strings.length != 0 &&
                (!StringUtils.isNumeric(strings[0])
                        || Integer.parseInt(strings[0]) < 1
                        || Integer.parseInt(strings[0]) > lastPage)) {

            fCommand.sendMeMessage("command.ignorelist.page-not-exist");
            return;
        }

        if (ignores == 0) {
            fCommand.sendMeMessage("command.ignorelist.empty");
            return;
        }

        if (fCommand.isHaveCD()) return;

        ComponentBuilder componentBuilder = new ComponentBuilder();

        String title = locale.getFormatString("command.ignorelist.title", commandSender)
                .replace("<count>", String.valueOf(ignores));

        componentBuilder.append(FComponent.fromLegacyText(title)).append("\n\n");

        String unignoreButton = locale.getFormatString("command.ignorelist.unignore-button", commandSender);

        int page = strings.length > 0 ? Math.max(1, Integer.parseInt(strings[0])) : 1;

        page = Math.min(lastPage, page);

        commandFPlayer.getIgnoreList().stream().skip((long) (page - 1) * perPage).limit(perPage).forEach(uuid ->  {

            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName == null) return;

            String playerIgnoreFormat = locale.getFormatString("command.ignorelist.player-ignore", commandSender)
                    .replace("<unignore>", unignoreButton)
                    .replace("<player>", playerName);

            String unbanHover = locale.getFormatString("command.ignorelist.unignore-hover", commandSender)
                    .replace("<player>", playerName);

            FComponent textComponent = new FComponent(playerIgnoreFormat)
                    .addHoverText(unbanHover)
                    .addRunCommand("/ignore " + playerName);

            componentBuilder
                    .append(textComponent.get())
                    .append("\n\n");
        });

        componentBuilder.append(new FListComponent("ignorelist", commandSender, page, lastPage).get());

        commandSender.spigot().sendMessage(componentBuilder.create());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            FPlayer fPlayer = FPlayerManager.getPlayer(commandSender.getName());
            if (fPlayer == null) return wordsList;

            int perPage = config.getInt("command.ignorelist.per-page");

            int lastPage = (int) Math.ceil((double) fPlayer.getIgnoreList().size() / perPage);

            isDigitInArray(strings[0], "", 1, lastPage + 1);
        }

        Collections.sort(wordsList);

        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "ignorelist";
    }
}
