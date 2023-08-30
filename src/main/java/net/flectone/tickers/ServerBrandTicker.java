package net.flectone.tickers;

import net.flectone.managers.FileManager;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.misc.brand.ServerBrand;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ServerBrandTicker extends FBukkitRunnable {

    private static final List<String> brands = new ArrayList<>();
    private static int index = 0;

    public ServerBrandTicker() {
        super.period = FileManager.config.getInt("server.brand.update.rate");

        brands.clear();
        brands.addAll(FileManager.locale.getStringList("server.brand.message"));
    }

    @Override
    public void run() {
        int nextIndex;
        if (!brands.isEmpty()) nextIndex = index++ % brands.size();
        else nextIndex = 0;

        Bukkit.getOnlinePlayers().parallelStream().forEach(player ->
                ServerBrand.getInstance().updateBrand(player, brands.get(nextIndex)));
    }
}
