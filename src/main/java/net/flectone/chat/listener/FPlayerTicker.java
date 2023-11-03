package net.flectone.chat.listener;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.FTicker;

import java.util.HashMap;

public class FPlayerTicker extends FTicker {
    public FPlayerTicker(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        super.delay = 20 * 60 * 60;
        super.period = delay;
        runTaskTimer();
    }

    @Override
    public void run() {
        FlectoneChat.getDatabase().execute(() ->
                FlectoneChat.getDatabase().clearExpiredData());

        var temp = new HashMap<>(FPlayerManager.getF_PLAYER_MAP());

        FPlayerManager.getF_PLAYER_MAP().forEach((key, fPlayer) -> {
            if (fPlayer == null || !fPlayer.getOfflinePlayer().isOnline()) {
                temp.remove(key);
            }
        });

        FPlayerManager.getF_PLAYER_MAP().clear();
        FPlayerManager.getF_PLAYER_MAP().putAll(temp);
    }
}
