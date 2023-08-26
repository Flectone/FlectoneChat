package net.flectone.tickers;

import net.flectone.managers.FPlayerManager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

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

        if (headerEnable) loadList(headers, "header");
        if (footerEnable) loadList(footers, "footer");
    }

    private void loadList(List<String> list, String tabType) {
        list.clear();

        List<String> stringList = locale.getStringList("tab." + tabType + ".message");

        StringBuilder stringBuilder = new StringBuilder();

        stringList.forEach(string -> {
            if (string.equals("<next>")) {
                list.add(stringBuilder.substring(0, stringBuilder.length() - 1));
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(string).append("\n");
            }
        });

        list.add(stringBuilder.substring(0, stringBuilder.length() - 1));
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            FPlayer fPlayer = FPlayerManager.getPlayer(player);
            if (fPlayer == null) return;

            fPlayer.updateName();

            if (headerEnable && !headers.isEmpty()) {
                int index = headerIndex++ % headers.size();
                String string = ObjectUtil.formatString(headers.get(index), player);
                player.setPlayerListHeader(string);
            }

            if (footerEnable && !footers.isEmpty()) {
                int index = footerIndex++ % footers.size();
                String string = ObjectUtil.formatString(footers.get(index), player);
                player.setPlayerListFooter(string);
            }
        });
    }
}
