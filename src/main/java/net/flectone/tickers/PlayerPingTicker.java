package net.flectone.tickers;

import net.flectone.custom.FBukkitRunnable;
import net.flectone.managers.FPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class PlayerPingTicker extends FBukkitRunnable {


    public static void registerPingObjective(){
        if(FPlayerManager.getScoreBoard().getObjective("ping") != null) return;
        Objective objective = FPlayerManager.getScoreBoard().registerNewObjective("ping", "dummy", "ping");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public static void unregisterPingObjective(){
        Objective objective = FPlayerManager.getScoreBoard().getObjective("ping");
        if(objective == null) return;
        objective.unregister();
    }

    public PlayerPingTicker() {
        super.period = 20L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            Objective objective = FPlayerManager.getScoreBoard().getObjective("ping");
            if(objective == null) return;
            Score score = objective.getScore(player.getName());

            score.setScore(player.getPing());
        });
    }
}
