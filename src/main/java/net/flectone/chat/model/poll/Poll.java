package net.flectone.chat.model.poll;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.PollManager;
import net.flectone.chat.model.file.FConfiguration;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Poll {

    private final List<FPlayer> fPlayerList = new ArrayList<>();
    private final String message;
    @Getter
    private final int id;
    private int agree = 0;
    private int disagree = 0;
    private boolean isExpired = false;

    public Poll(@Nullable Player commandSender, @NotNull String message) {
        this.message = message;

        FlectoneChat plugin = FlectoneChat.getPlugin();

        FConfiguration commands = plugin.getFileManager().getCommands();

        this.id = commands.getInt("poll.last-id") + 1;
        int time = commands.getInt("poll.time");
        PollManager.add(this);

        Bukkit.getScheduler().runTaskLater(FlectoneChat.getPlugin(), () -> {
            isExpired = true;

            FConfiguration locale = plugin.getFileManager().getLocale();
            String formatMessage = locale.getVaultString(commandSender, "commands.poll.over-message")
                    .replace("<id>", String.valueOf(id))
                    .replace("<message>", message)
                    .replace("<agree>", locale.getVaultString(commandSender, "commands.poll.format.agree"))
                    .replace("<agree_count>", String.valueOf(agree))
                    .replace("<disagree>", locale.getVaultString(commandSender, "commands.poll.format.disagree"))
                    .replace("<disagree_count>", String.valueOf(disagree));

            Bukkit.getOnlinePlayers().parallelStream()
                    .forEach(player -> player.sendMessage(MessageUtil.formatAll(commandSender, player,formatMessage)));

        }, time);
    }

    public int vote(@NotNull FPlayer fPlayer, @NotNull String typeVote) {
        if (fPlayerList.contains(fPlayer)) return 0;

        fPlayerList.add(fPlayer);

        if (typeVote.equalsIgnoreCase("agree")) return ++agree;
        else return ++disagree;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public boolean isExpired() {
        return isExpired;
    }

}
