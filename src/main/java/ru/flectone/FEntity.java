package ru.flectone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class FEntity {


    private static HashMap<String, Team> entityNoCollisionTeamHashMap = new HashMap<>();

    public static void addEntityToENT(Entity entity, String color){
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(color) != null)
            entityNoCollisionTeamHashMap.put(color, Bukkit.getScoreboardManager().getMainScoreboard().getTeam(color));

        Team team = entityNoCollisionTeamHashMap.get(color);

        if(team == null){

            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(color);

            team.setCanSeeFriendlyInvisibles(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setColor(ChatColor.valueOf(color));

            entityNoCollisionTeamHashMap.put(color, team);
        }

        if(entity instanceof Player){
            team.addEntry(((Player) entity).getName());
        } else team.addEntry(entity.getUniqueId().toString());
    }

    public static void leaveEntityTeam(Entity entity, String color){
        Team team = entityNoCollisionTeamHashMap.get(color);

        if(entity instanceof Player){
            team.removeEntry(((Player) entity).getName());
        } else team.removeEntry(entity.getUniqueId().toString());
    }

}
