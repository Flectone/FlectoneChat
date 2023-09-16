package net.flectone.misc.runnables;

import net.flectone.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FBukkitRunnable extends BukkitRunnable {

    protected long delay = 0L;
    protected long period;

    @Override
    public void run() {
    }

    public void runTaskTimer() {
        super.runTaskTimer(Main.getInstance(), delay, period);
    }

    protected void loadLocaleList(List<String> list, String localeString) {
        list.clear();

        List<String> stringList = locale.getStringList(localeString);

        if (stringList.isEmpty()) {
            list.add("update message to list");
            return;
        }

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
}
