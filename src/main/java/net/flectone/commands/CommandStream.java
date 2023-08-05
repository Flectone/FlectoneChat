package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.utils.ObjectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandStream extends FTabCompleter {

    public CommandStream() {
        super.commandName = "stream";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (strings.length < 1 || !strings[0].equalsIgnoreCase("start") && !strings[0].equalsIgnoreCase("end")) {
            fCommand.sendUsageMessage();
            return true;
        }

        if (strings.length == 1 && strings[0].equalsIgnoreCase("start")) {
            fCommand.sendMeMessage("command.stream.start.need-url");
            return true;
        }

        if (!fCommand.isConsole()) {
            if (!fCommand.getFPlayer().isStreaming() && strings[0].equalsIgnoreCase("end")) {
                fCommand.sendMeMessage("command.stream.end.not");
                return true;
            }

            if (fCommand.getFPlayer().isStreaming() && strings[0].equalsIgnoreCase("start")) {
                fCommand.sendMeMessage("command.stream.start.already");
                return true;
            }

            if (strings[0].equalsIgnoreCase("end")) {
                fCommand.getFPlayer().setStreaming(false);
                fCommand.getFPlayer().setDisplayName();
                fCommand.sendMeMessage("command.stream.end.message");
                return true;
            }

            if (fCommand.isHaveCD()) return true;

            if (fCommand.isMuted()) return true;

            fCommand.getFPlayer().setStreaming(true);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : Main.locale.getStringList("command.stream.start.message")) {

            string = string
                    .replace("<player>", fCommand.getSenderName())
                    .replace("<links>", ObjectUtil.toString(strings, 1, "\n") + " ");

            stringBuilder.append(string);
            stringBuilder.append("\n");
        }

        FDiscordSRV.sendModerationMessage(stringBuilder.toString());

        fCommand.sendGlobalMessage(stringBuilder.toString());
        fCommand.getFPlayer().setDisplayName();

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "start");
            isStartsWith(strings[0], "end");
        } else if (strings.length == 2 && strings[0].equalsIgnoreCase("start")) {
            isStartsWith(strings[1], "https://flectone.net");
        }

        Collections.sort(wordsList);

        return wordsList;
    }
}
