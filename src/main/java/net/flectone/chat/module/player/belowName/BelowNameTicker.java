package net.flectone.chat.module.player.belowName;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BelowNameTicker extends FTicker {

    public BelowNameTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.period = 20L;
        registerObjective();
        runTaskTimer();
    }

    public void registerObjective() {
        Scoreboard scoreboard = FlectoneChat.getPlugin().getScoreBoard();
        if (scoreboard.getObjective("belowName") != null) return;
        Objective objective = scoreboard.registerNewObjective("belowName", "dummy", "FlectoneChat");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public void unregisterObjective() {
        Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("belowName");
        if (objective == null) return;
        objective.unregister();
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Objective objective = FlectoneChat.getPlugin().getScoreBoard().getObjective("belowName");
            if (objective == null) return;

            String message = locale.getVaultString(player, getModule() + ".message");
            objective.setDisplayName(MessageUtil.formatAll(player, message));

            Score score = objective.getScore(player.getName());
            String mode = config.getVaultString(player, getModule() + ".mode");
            score.setScore(PlayerUtil.getObjectiveScore(player, mode));
        });
    }

    @Override
    public void cancel() {
        unregisterObjective();
        super.cancel();
    }
}
