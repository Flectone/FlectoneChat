package net.flectone.chat.module.player.afkTimeout;

import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.Pair;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class AfkTimeoutModule extends FModule {

    private final HashMap<UUID, Pair<Integer, Block>> LAST_BLOCK_MAP = new HashMap<>();

    public AfkTimeoutModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new AfkTimeoutTicker(this));
        FActionManager.add(new AfkTimeoutListener(this));
    }

    public void setAfk(@NotNull Player player, boolean isAfk, @NotNull String takeOutAction) {
        if (!isAfk) {
            List<String> list = config.getVaultStringList(player, this + ".take-out-actions");
            if (!list.contains(takeOutAction)) return;

            LAST_BLOCK_MAP.put(player.getUniqueId(), null);
        } else {

            LAST_BLOCK_MAP.put(player.getUniqueId(), new Pair<>(TimeUtil.getCurrentTime(), player.getLocation().getBlock()));
        }

        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer == null) return;
        if (fPlayer.isAfk() == isAfk) return;

        String afkSuffix = isAfk
                ? locale.getVaultString(player, "commands.afk.suffix")
                : "";

        fPlayer.setAfkSuffix(MessageUtil.formatAll(player, afkSuffix));

        String afkMessage = locale.getVaultString(player, "commands.afk." + isAfk + "-message");
        player.sendMessage(MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, afkMessage)));

        fPlayer.playSound(player, player, this.toString());
    }

    public void checkAfk(@NotNull Player player) {
        FPlayer fPlayer = FPlayerManager.get(player);
        if(fPlayer == null) return;

        if (!isMoved(player)) {

            if (fPlayer.isAfk()) return;

            int afkTimeout = config.getVaultInt(player, this + ".time");
            if (afkTimeout != 0) afkTimeout /= 20;

            if (getAfkTime(player) >= afkTimeout) {
                setAfk(player, true, "move");
                fPlayer.playSound(player, player, this + "-true");
            }

            return;
        }

        Block block = player.getLocation().getBlock();
        LAST_BLOCK_MAP.put(player.getUniqueId(), new Pair<>(TimeUtil.getCurrentTime(), block));

        if (!fPlayer.isAfk()) return;

        setAfk(player, false, "move");
        fPlayer.playSound(player, player, this + "-false");
    }

    public void removePlayer(@NotNull UUID uuid) {
        LAST_BLOCK_MAP.remove(uuid);
    }

    public boolean isMoved(@NotNull Player player) {
        if (LAST_BLOCK_MAP.get(player.getUniqueId()) == null) return true;

        Block lastBlock = LAST_BLOCK_MAP.get(player.getUniqueId()).getValue();

        return !lastBlock.equals(player.getLocation().getBlock());
    }

    public int getAfkTime(@NotNull Player player) {
        int lastMovedTime = LAST_BLOCK_MAP.get(player.getUniqueId()).getKey();
        return TimeUtil.getCurrentTime() - lastMovedTime;
    }
}
