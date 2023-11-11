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

    private final List<FListener> F_LISTENER_LIST = new ArrayList<>();
    private final List<FTicker> F_TICKER_LIST = new ArrayList<>();
    private final List<FInfo> F_INFO_LIST = new ArrayList<>();
    private final List<FCommand> F_COMMAND_LIST = new ArrayList<>();

    public FActionManager() {}

    public void add(FCommand fCommand) {
        F_COMMAND_LIST.add(fCommand);
    }

    public void add(FListener fListener) {
        F_LISTENER_LIST.add(fListener);
    }

    public void add(FTicker fTicker) {
        F_TICKER_LIST.add(fTicker);
    }

    public void add(FInfo fInfo) {
        F_INFO_LIST.add(fInfo);
    }

    public void clearAll() {
        clearCommands();
        clearInfos();
        clearListeners();
        clearTickers();
    }

    public void clearCommands() {
        F_COMMAND_LIST.forEach(fCommand ->
                CommandsUtil.unregisterCommand(fCommand.getCommand()));
    }

    public void clearInfos() {
        F_INFO_LIST.clear();
    }

    public void clearListeners() {
        HandlerList.unregisterAll(FlectoneChat.getPlugin());
        F_LISTENER_LIST.clear();
    }

    public void clearTickers() {
        if (F_TICKER_LIST.isEmpty()) return;
        F_TICKER_LIST.stream().filter(fTicker -> !fTicker.isCancelled()).forEach(BukkitRunnable::cancel);

        F_TICKER_LIST.clear();
    }
}
