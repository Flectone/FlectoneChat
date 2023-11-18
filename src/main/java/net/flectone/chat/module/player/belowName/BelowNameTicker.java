package net.flectone.chat.module.player.belowName;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

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
            score.setScore(getObjectiveScore(player));
        });
    }

    public int getObjectiveScore(@NotNull Player player) {
        return switch (config.getVaultString(player, getModule() + ".mode").toLowerCase()) {
            case "health" -> (int) Math.round(player.getHealth() * 10.0)/10;
            case "level" -> player.getLevel();
            case "food" -> player.getFoodLevel();
            case "ping" -> player.getPing();
            case "armor" -> {
                AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
                yield armor != null ? (int) Math.round(armor.getValue() * 10.0)/10 : 0;
            }
            case "attack" -> {
                AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                yield damage != null ? (int) Math.round(damage.getValue() * 10.0)/10 : 0;
            }
            default -> 0;
        };
    }

    @Override
    public void cancel() {
        unregisterObjective();
        super.cancel();
    }
}
