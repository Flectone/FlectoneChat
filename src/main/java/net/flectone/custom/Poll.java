package net.flectone.custom;

import net.flectone.Main;
import net.flectone.managers.PollManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class Poll {

    private final List<FPlayer> fPlayerList = new ArrayList<>();

    private final String message;

    private int agree = 0;

    private int disagree = 0;

    private boolean isExpired = false;

    private final int id;

    public Poll(String message) {
        this.message = message;

        this.id = PollManager.getPollList().size();
        PollManager.add(this);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            isExpired = true;

            String formatMessage = Main.locale.getString("command.poll.over-message")
                    .replace("<id>", String.valueOf(id))
                    .replace("<message>", message)
                    .replace("<agree>", Main.locale.getString("command.poll.format.agree"))
                    .replace("<agree_count>", String.valueOf(agree))
                    .replace("<disagree>", Main.locale.getString("command.poll.format.disagree"))
                    .replace("<disagree_count>", String.valueOf(disagree));

            Bukkit.getOnlinePlayers().parallelStream()
                    .forEach(player -> player.sendMessage(ObjectUtil.formatString(formatMessage, player)));

        }, 20L * Main.config.getInt("command.poll.time"));
    }

    public int vote(FPlayer fPlayer, String typeVote) {
        if (fPlayerList.contains(fPlayer)) return 0;

        fPlayerList.add(fPlayer);

        if (typeVote.equalsIgnoreCase("agree")) return ++agree;
        else return ++disagree;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public boolean isExpired() {
        return isExpired;
    }

}
