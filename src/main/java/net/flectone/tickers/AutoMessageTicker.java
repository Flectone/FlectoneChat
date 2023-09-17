package net.flectone.tickers;

import net.flectone.managers.FileManager;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.runnables.FBukkitRunnable;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class AutoMessageTicker extends FBukkitRunnable {

    private final boolean random;
    private static final List<String> messageList = new ArrayList<>();
    private static int index = 0;

    public AutoMessageTicker() {
        int count = FileManager.config.getInt("chat.auto-message.period");
        super.period = count;
        super.delay = count;
        this.random = FileManager.config.getBoolean("chat.auto-message.random");

        loadLocaleList(messageList, "chat.auto-message.message");
    }

    @Override
    public void run() {
        int nextIndex;

        nextIndex = random
                ? ObjectUtil.nextInt(0, messageList.size())
                : !messageList.isEmpty()
                    ? index++ % messageList.size()
                    : 0;

        FCommand fCommand = new FCommand(Bukkit.getConsoleSender(), "auto-message", "", new String[]{});
        fCommand.sendGlobalMessage(messageList.get(nextIndex), "", null, false);
    }
}
