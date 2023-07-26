package net.flectone.tickers;

import net.flectone.custom.FBukkitRunnable;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class PlayerPingTicker extends FBukkitRunnable {

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
