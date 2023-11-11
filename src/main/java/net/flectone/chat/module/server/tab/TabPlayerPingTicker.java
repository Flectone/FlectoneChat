package net.flectone.chat.module.server.tab;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class TabPlayerPingTicker extends FTicker {

    public TabPlayerPingTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = 20L;
        registerPingObjective();
        runTaskTimer();
    }

    public static void registerPingObjective() {
        Scoreboard scoreboard = FlectoneChat.getPlugin().getScoreBoard();
        if (scoreboard.getObjective("ping") != null) return;
        Objective objective = scoreboard.registerNewObjective("ping", "dummy", "ping");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public static void unregisterPingObjective() {
        Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("ping");
        if (objective == null) return;
        objective.unregister();
    }

    @Override
    public void run() {
        PlayerUtil.getPlayersWithFeature(getModule() + ".player-ping.enable").forEach(player -> {
            Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("ping");
            if (objective == null) return;
            Score score = objective.getScore(player.getName());

            score.setScore(player.getPing());
        });
    }

    @Override
    public void cancel() {
        unregisterPingObjective();
        super.cancel();
    }
}