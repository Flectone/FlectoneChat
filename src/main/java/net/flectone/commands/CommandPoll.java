package net.flectone.commands;

import net.flectone.Main;
import net.flectone.custom.FCommands;
import net.flectone.custom.FTabCompleter;
import net.flectone.custom.Poll;
import net.flectone.managers.PollManager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CommandPoll extends FTabCompleter {

    public CommandPoll() {
        super.commandName = "poll";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FCommands fCommand = new FCommands(commandSender, command.getName(), s, strings);

        if (fCommand.isInsufficientArgs(2)) return true;

        if (!strings[0].equalsIgnoreCase("vote") && !strings[0].equalsIgnoreCase("create")) {
            fCommand.sendUsageMessage();
            return true;
        }

        if (fCommand.isHaveCD() || fCommand.isMuted()) return true;

        if (strings[0].equalsIgnoreCase("create")) {

            if (!commandSender.hasPermission(Main.config.getString("command.poll.permission"))
                    && !commandSender.isOp()) {
                fCommand.sendMeMessage("command.poll.permission-message");
                return true;
            }

            Poll poll = new Poll(ObjectUtil.toString(strings, 1));

            String formatString = Main.locale.getString("command.poll.message")
                    .replace("<id>", String.valueOf(poll.getId()));

            fCommand.sendGlobalMessage(new HashSet<>(Bukkit.getOnlinePlayers()), formatString, poll.getMessage(), null, false);

            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder
                    .append(createVoteComponent("agree", poll.getId()))
                    .append(" ", ComponentBuilder.FormatRetention.NONE)
                    .append(createVoteComponent("disagree", poll.getId()));

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

        if (poll.isExpired()) {
            fCommand.sendMeMessage("command.poll.expired-message");
            return true;
        }

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

    private TextComponent createVoteComponent(String voteType, int id) {
        String agreeString = Main.locale.getFormatString("command.poll.format." + voteType, null);
        TextComponent voteComponent = new TextComponent(TextComponent.fromLegacyText(agreeString));
        voteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/poll vote " + id + " " + voteType));
        return voteComponent;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        wordsList.clear();

        if (strings.length == 1) {
            isStartsWith(strings[0], "vote");
            if (commandSender.hasPermission(Main.config.getString("command.poll.permission")) || commandSender.isOp()) {
                isStartsWith(strings[0], "create");
            }
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("create")) {
                isStartsWith(strings[1], "(message)");
            } else {
                PollManager.getPollList()
                        .parallelStream()
                        .filter(poll -> !poll.isExpired())
                        .forEach(poll -> isStartsWith(strings[1], String.valueOf(poll.getId())));
            }
        } else if (strings.length == 3 && strings[0].equalsIgnoreCase("vote")) {
            isStartsWith(strings[2], "agree");
            isStartsWith(strings[2], "disagree");
        }

        Collections.sort(wordsList);
        return wordsList;
    }
}
