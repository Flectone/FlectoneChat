package net.flectone.tickers;

import net.flectone.Main;
import net.flectone.commands.CommandAfk;
import net.flectone.custom.FBukkitRunnable;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class AfkTicker extends FBukkitRunnable {

    public AfkTicker(){
        super.period = 20L;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);

            Block block = fPlayer.getPlayer().getLocation().getBlock();

            if(!fPlayer.isMoved(block)){

                boolean isEnable = Main.config.getBoolean("command.afk.timeout.enable");
                if(fPlayer.isAfk() || !isEnable) return;

                int diffTime = ObjectUtil.getCurrentTime() - fPlayer.getLastTimeMoved();

                if(diffTime >= Main.config.getInt("command.afk.timeout.time")){
                    CommandAfk.sendMessage(fPlayer, true);
                }

                return;
            }

            fPlayer.setBlock(block);

            if(!fPlayer.isAfk()) return;

            CommandAfk.sendMessage(fPlayer, false);
        });
    }
}
