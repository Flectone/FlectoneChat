package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class AfkTicker extends FBukkitRunnable {

    public AfkTicker() {
        super.period = 20L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if(fPlayer == null) return;

            Block block = player.getLocation().getBlock();

            if (!fPlayer.isMoved(block)) {

                boolean isEnable = Main.config.getBoolean("command.afk.timeout.enable");
                if (fPlayer.isAfk() || !isEnable) return;

                int diffTime = ObjectUtil.getCurrentTime() - fPlayer.getLastTimeMoved();

                if (diffTime >= Main.config.getInt("command.afk.timeout.time")) {
                    CommandAfk.sendMessage(fPlayer, true);
                    fPlayer.setDisplayName();
                }

                return;
            }

            fPlayer.setBlock(block);

            if (!fPlayer.isAfk()) return;

            CommandAfk.sendMessage(fPlayer, false);
            fPlayer.setDisplayName();
        });
    }
}
