package net.flectone.chat.module.player.nameTag;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.flectone.chat.manager.FileManager.config;

public class NameTagModule extends FModule {

    public NameTagModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @NotNull
    public Team getTeam(@NotNull Player player) {
        String playerName = player.getName();
        String sortName = playerName;
        if (config.getVaultBoolean(player, this + ".sort.enable")) {
            sortName = getSortName(player);
        }

        Team bukkitTeam = FlectoneChat.getScoreBoard().getTeam(sortName);

        Team team = bukkitTeam != null
                ? bukkitTeam
                : FlectoneChat.getScoreBoard().registerNewTeam(sortName);

        if (!team.hasEntry(playerName)) {
            team.addEntry(playerName);
        }

        setVisibility(player, team);

        team.setColor(ChatColor.WHITE);

        FModule fModule = FlectoneChat.getModuleManager().get(NameModule.class);
        if (!(fModule instanceof NameModule nameModule)) return team;

        if (config.getVaultBoolean(player, this + ".prefix.enable")) {
            team.setPrefix(nameModule.getPrefix(player));
        }

        if (config.getVaultBoolean(player, this + ".suffix.enable")) {
            team.setSuffix(nameModule.getSuffix(player));
        }

        return team;
    }

    @NotNull
    public String getSortName(@NotNull Player player) {
        String playerName = player.getName();
        if (Bukkit.getBukkitVersion().startsWith("1.16.5")) return playerName;
        if (!config.getVaultBoolean(player, this + ".sort.enable")) return playerName;
        if (hasNoPermission(player, "sort")) return playerName;

        int rank = IntegrationsModule.getPrimaryGroupWeight(player);

        return PlayerUtil.generateSortString(rank, player.getName());
    }

    public void setVisibility(@Nullable Player player, @Nullable Team team) {
        if (player == null) return;
        if (team == null) return;
        if (hasNoPermission(player, "visible")) return;

        boolean isVisible = config.getVaultBoolean(player, this + ".visible.enable");

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, isVisible
                ? Team.OptionStatus.ALWAYS
                : Team.OptionStatus.NEVER);
    }
}
