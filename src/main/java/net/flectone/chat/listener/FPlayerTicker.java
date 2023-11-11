package net.flectone.chat.listener;

import net.flectone.chat.FlectoneChat;
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
        FlectoneChat.getPlugin().getDatabase().execute(() ->
                FlectoneChat.getPlugin().getDatabase().clearExpiredData());

        var temp = new HashMap<>(playerManager.getF_PLAYER_MAP());

        playerManager.getF_PLAYER_MAP().forEach((key, fPlayer) -> {
            if (fPlayer == null || !fPlayer.getOfflinePlayer().isOnline()) {
                temp.remove(key);
            }
        });

        playerManager.getF_PLAYER_MAP().clear();
        playerManager.getF_PLAYER_MAP().putAll(temp);
    }
}
