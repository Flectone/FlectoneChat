package net.flectone.tickers;

import net.flectone.custom.FBukkitRunnable;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class PlayerPingTicker extends FBukkitRunnable {

    static {
        Objective objective = FPlayerManager.getScoreBoard().registerNewObjective("ping", Criteria.DUMMY, "ping");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public PlayerPingTicker() {
        super.period = 20L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            Objective objective = FPlayerManager.getScoreBoard().getObjective("ping");
            Score score = objective.getScore(player.getName());

            score.setScore(player.getPing());

            player.setScoreboard(objective.getScoreboard());
        });
    }
}
