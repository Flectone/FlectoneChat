package net.flectone.chat.manager;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FCommand;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FTicker;
import net.flectone.chat.util.CommandsUtil;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class FActionManager {

    private final List<FListener> listenerList = new ArrayList<>();
    private final List<FTicker> tickerList = new ArrayList<>();
    private final List<FCommand> commandList = new ArrayList<>();

    public void add(FCommand fCommand) {
        commandList.add(fCommand);
    }

    public void add(FListener fListener) {
        listenerList.add(fListener);
    }

    public void add(FTicker fTicker) {
        tickerList.add(fTicker);
    }


    public void clearAll() {
        clearCommands();
        clearListeners();
        clearTickers();
    }

    public void clearCommands() {
        commandList.forEach(fCommand ->
                CommandsUtil.unregisterCommand(fCommand.getCommand()));
    }
    
    public void clearListeners() {
        HandlerList.unregisterAll(FlectoneChat.getPlugin());
        listenerList.clear();
    }

    public void clearTickers() {
        if (tickerList.isEmpty()) return;
        tickerList.stream().filter(fTicker -> !fTicker.isCancelled()).forEach(BukkitRunnable::cancel);

        tickerList.clear();
    }
}
