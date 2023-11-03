package net.flectone.chat.manager;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FInfo;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.CommandsUtil;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class FActionManager {

    private static final List<FListener> F_LISTENER_LIST = new ArrayList<>();
    private static final List<FTicker> F_TICKER_LIST = new ArrayList<>();
    private static final List<FInfo> F_INFO_LIST = new ArrayList<>();
    private static final List<FCommand> F_COMMAND_LIST = new ArrayList<>();

    public static void add(FCommand fCommand) {
        F_COMMAND_LIST.add(fCommand);
    }

    public static void add(FListener fListener) {
        F_LISTENER_LIST.add(fListener);
    }

    public static void add(FTicker fTicker) {
        F_TICKER_LIST.add(fTicker);
    }

    public static void add(FInfo fInfo) {
        F_INFO_LIST.add(fInfo);
    }

    public static void clearAll() {
        clearCommands();
        clearInfos();
        clearListeners();
        clearTickers();
    }

    public static void clearCommands() {
        F_COMMAND_LIST.forEach(fCommand ->
                CommandsUtil.unregisterCommand(fCommand.getCommand()));
    }

    public static void clearInfos() {
        F_INFO_LIST.clear();
    }

    public static void clearListeners() {
        HandlerList.unregisterAll(FlectoneChat.getInstance());
        F_LISTENER_LIST.clear();
    }

    public static void clearTickers() {
        if (F_TICKER_LIST.isEmpty()) return;
        F_TICKER_LIST.stream().filter(fTicker -> !fTicker.isCancelled()).forEach(BukkitRunnable::cancel);

        F_TICKER_LIST.clear();
    }
}
