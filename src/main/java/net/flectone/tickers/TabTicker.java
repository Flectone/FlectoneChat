package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;

public class TabTicker extends FBukkitRunnable {

    private static final List<String> headers = new ArrayList<>();
    private static final List<String> footers = new ArrayList<>();
    private static int headerIndex = 0;
    private static int footerIndex = 0;

    private static final boolean headerEnable = config.getBoolean("tab.header-message.enable");
    private static final boolean footerEnable = config.getBoolean("tab.footer-message.enable");

    public TabTicker() {
        super.period = config.getInt("tab.update.rate");
        headerIndex = 0;
        footerIndex = 0;

        if (headerEnable) loadLocaleList(headers, "tab.header.message");
        if (footerEnable) loadLocaleList(footers, "tab.footer.message");
    }

    @Override
    public void run() {
        int nextHeaderIndex;
        if (!headers.isEmpty()) nextHeaderIndex = headerIndex++ % headers.size();
        else nextHeaderIndex = 0;

        int nextFooterIndex;
        if (!footers.isEmpty()) nextFooterIndex = footerIndex++ % footers.size();
        else nextFooterIndex = 0;

        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) return;

            fPlayer.updateName();

            if (headerEnable && !headers.isEmpty()) {
                String string = ObjectUtil.formatString(headers.get(nextHeaderIndex), player);
                player.setPlayerListHeader(string);
            }

            if (footerEnable && !footers.isEmpty()) {
                String string = ObjectUtil.formatString(footers.get(nextFooterIndex), player);
                player.setPlayerListFooter(string);
            }
        });
    }
}
