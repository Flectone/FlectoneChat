package net.flectone.custom;

import net.flectone.managers.FPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class FEntity {

    private static final HashMap<String, Team> noCollisionTeamMap = new HashMap<>();

    public static void addToTeam(Entity entity, String color) {
        if (entity instanceof Player) {

            Player player = (Player) entity;
            FPlayerManager.getPlayer(player).setTeamColor(color);
            return;
        }

        if (FPlayerManager.getScoreBoard().getTeam(color) != null)
            noCollisionTeamMap.put(color, FPlayerManager.getScoreBoard().getTeam(color));

        Team team = noCollisionTeamMap.get(color);

        if (team == null) {

            team = FPlayerManager.getScoreBoard().registerNewTeam(color);

            team.setCanSeeFriendlyInvisibles(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setColor(ChatColor.valueOf(color));

            noCollisionTeamMap.put(color, team);
        }

        team.addEntry(entity.getUniqueId().toString());
    }

    public static void removeFromTeam(Entity entity, String color) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            FPlayerManager.getScoreBoard()
                    .getTeam(player.getName())
                    .setColor(ChatColor.WHITE);
            return;
        }

        Team team = noCollisionTeamMap.get(color);
        team.removeEntry(entity.getUniqueId().toString());
    }

    public static void removePlayerFromTeam(FPlayer fPlayer) {
        Team team = FPlayerManager.getScoreBoard().getTeam(fPlayer.getRealName());
        if (team == null) return;
        team.removeEntry(fPlayer.getRealName());
    }

    public static void removeBugEntities(Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20, Entity::isGlowing).forEach(entity -> {
            if (entity.isSilent() && entity.isInvulnerable() && !entity.isVisualFire()) entity.remove();

            entity.setGlowing(false);
        });
    }

}
