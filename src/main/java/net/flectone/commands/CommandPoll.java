package net.flectone.commands;

import net.flectone.managers.PollManager;
import net.flectone.misc.actions.Poll;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.commands.FTabCompleter;
import net.flectone.misc.components.FVoteComponent;
import net.flectone.utils.ObjectUtil;
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

public class CommandPoll implements FTabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommand fCommand = new FCommand(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return true;

        if (!strings[0].equalsIgnoreCase("vote") && !strings[0].equalsIgnoreCase("create")) {
            fCommand.sendUsageMessage();
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (strings[0].equalsIgnoreCase("create")) {

            if (!commandSender.hasPermission(config.getString("command.poll.permission"))
                    && !commandSender.isOp()) {
                fCommand.sendMeMessage("command.poll.permission-message");
                return true;
            }

            Poll poll = new Poll(ObjectUtil.toString(strings, 1));

            String formatString = locale.getString("command.poll.message")
                    .replace("<id>", String.valueOf(poll.getId()));

            fCommand.sendGlobalMessage(formatString, poll.getMessage(), null, false);

            String voteId = String.valueOf(poll.getId());

            ComponentBuilder componentBuilder = new ComponentBuilder();

            componentBuilder
                    .append(new FVoteComponent( "agree", voteId).get())
                    .append(" ", ComponentBuilder.FormatRetention.NONE)
                    .append(new FVoteComponent("disagree", voteId).get());

            Bukkit.getOnlinePlayers().parallelStream()
                    .forEach(player -> player.spigot().sendMessage(componentBuilder.create()));

            return true;
        }

        if (strings.length < 3 || !StringUtils.isNumeric(strings[1]) || PollManager.getPollList().size() < Integer.parseInt(strings[1])
                || (!strings[2].equalsIgnoreCase("agree") && !strings[2].equalsIgnoreCase("disagree"))) {
            fCommand.sendUsageMessage();
            return true;
        }

        int id = Integer.parseInt(strings[1]);
        Poll poll = PollManager.get(id);

        if (poll == null || poll.isExpired()) {
            fCommand.sendMeMessage("command.poll.expired-message");
            return true;
        }

        if (fCommand.getFPlayer() == null) return true;

        int voteCounts = poll.vote(fCommand.getFPlayer(), strings[2]);
        if (voteCounts == 0) {
            fCommand.sendMeMessage("command.poll.already-message");
            return true;
        }

        String[] replaceStrings = {"<vote_type>", "<id>", "<count>"};
        String[] toStrings = {strings[2], String.valueOf(id), String.valueOf(voteCounts)};

        fCommand.sendMeMessage("command.poll." + strings[2].toLowerCase() + "-message", replaceStrings, toStrings);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        switch (strings.length) {
            case 1 -> {
                isStartsWith(strings[0], "vote");

                if (commandSender.hasPermission(config.getString("command.poll.permission")) || commandSender.isOp())
                    isStartsWith(strings[0], "create");
            }
            case 2 -> {
                if (strings[0].equalsIgnoreCase("create")) isTabCompleteMessage(strings[1]);
                else PollManager.getPollList()
                            .parallelStream()
                            .filter(poll -> !poll.isExpired())
                            .forEach(poll -> isStartsWith(strings[1], String.valueOf(poll.getId())));
            }
            case 3 -> {
                if (strings[0].equalsIgnoreCase("vote")) {
                    isStartsWith(strings[2], "agree");
                    isStartsWith(strings[2], "disagree");
                }
            }
        }

        Collections.sort(wordsList);
        return wordsList;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "poll";
    }
}
