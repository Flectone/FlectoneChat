package net.flectone.chat.module.server.tab.playerList;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerListTicker extends FTicker {

    public PlayerListTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = 20L;
        registerPingObjective();
        runTaskTimer();
    }

    public void registerPingObjective() {
        Scoreboard scoreboard = FlectoneChat.getPlugin().getScoreBoard();
        if (scoreboard.getObjective("playerList") != null) return;
        Objective objective = scoreboard.registerNewObjective("playerList", "dummy", "FlectoneChat");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public void unregisterPingObjective() {
        Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("playerList");
        if (objective == null) return;
        objective.unregister();
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("playerList");
            if (objective == null) return;
            Score score = objective.getScore(player.getName());

            String mode = config.getVaultString(player, getModule() + ".mode");
            score.setScore(PlayerUtil.getObjectiveScore(player, mode));
        });
    }

    @Override
    public void cancel() {
        unregisterPingObjective();
        super.cancel();
    }
}