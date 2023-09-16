package net.flectone.tickers;

import net.flectone.managers.FileManager;
import net.flectone.messages.MessageBuilder;
import net.flectone.misc.runnables.FBukkitRunnable;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoMessageTicker extends FBukkitRunnable {

    private final boolean random;
    private static final List<String> messageList = new ArrayList<>();
    private static int index = 0;

    public AutoMessageTicker() {
        super.period = FileManager.config.getInt("chat.auto-message.period");
        this.random = FileManager.config.getBoolean("chat.auto-message.random");

        loadLocaleList(messageList, "chat.auto-message.message");
    }

    @Override
    public void run() {
        int nextIndex;

        nextIndex = random
                ? new Random().nextInt(0, messageList.size())
                : !messageList.isEmpty()
                    ? index++ % messageList.size()
                    : 0;

        MessageBuilder message = new MessageBuilder("auto-message", messageList.get(nextIndex), Bukkit.getConsoleSender(), null, false);
        Bukkit.getOnlinePlayers().parallelStream().forEach(player -> {
            player.spigot().sendMessage(message.buildMessage("", player, Bukkit.getConsoleSender()));
        });
    }
}
