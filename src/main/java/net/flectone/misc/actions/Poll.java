package net.flectone.misc.actions;

import net.flectone.Main;
import net.flectone.managers.PollManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;
import static net.flectone.managers.FileManager.config;

public class Poll {

    private final List<FPlayer> fPlayerList = new ArrayList<>();
    private final String message;
    private final int id;
    private int agree = 0;
    private int disagree = 0;
    private boolean isExpired = false;

    public Poll(@NotNull String message) {
        this.message = message;

        this.id = PollManager.getPollList().size();
        PollManager.add(this);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            isExpired = true;

            String formatMessage = locale.getString("command.poll.over-message")
                    .replace("<id>", String.valueOf(id))
                    .replace("<message>", message)
                    .replace("<agree>", locale.getString("command.poll.format.agree"))
                    .replace("<agree_count>", String.valueOf(agree))
                    .replace("<disagree>", locale.getString("command.poll.format.disagree"))
                    .replace("<disagree_count>", String.valueOf(disagree));

            Bukkit.getOnlinePlayers().parallelStream()
                    .forEach(player -> player.sendMessage(ObjectUtil.formatString(formatMessage, player)));

        }, 20L * config.getInt("command.poll.time"));
    }

    public int vote(@NotNull FPlayer fPlayer, @NotNull String typeVote) {
        if (fPlayerList.contains(fPlayer)) return 0;

        fPlayerList.add(fPlayer);

        if (typeVote.equalsIgnoreCase("agree")) return ++agree;
        else return ++disagree;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public boolean isExpired() {
        return isExpired;
    }

}
