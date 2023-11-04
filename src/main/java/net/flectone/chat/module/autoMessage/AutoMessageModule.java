package net.flectone.chat.module.autoMessage;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.model.sound.FSound;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.sounds.SoundsModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import net.flectone.chat.util.RandomUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class AutoMessageModule extends FModule {

    private static final HashMap<Player, Integer> MESSAGE_INDEX_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> MESSAGE_MAP = new HashMap<>();

    public AutoMessageModule(String name) {
        super(name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new AutoMessageTicker(this));
    }

    public void send(@NotNull Player player, @NotNull String string) {
        player.sendMessage(string);
    }

    public void send(@NotNull Player player) {
        FPlayer fPlayer = FPlayerManager.get(player);
        if (fPlayer == null) return;

        String autoMessage = fPlayer.getSettings().getValue(Settings.Type.AUTO_MESSAGE);
        boolean enabled = autoMessage == null || Integer.parseInt(autoMessage) != -1;
        if (!enabled) return;

        send(player, incrementIndexAndGet(MESSAGE_MAP, MESSAGE_INDEX_MAP, player));

        FModule fModule = FlectoneChat.getModuleManager().get(SoundsModule.class);
        if (fModule instanceof SoundsModule soundsModule) {
            soundsModule.play(new FSound(player, player, this.toString()));
        }
    }

    @NotNull
    private String incrementIndexAndGet(@NotNull HashMap<String, List<String>> map, @NotNull HashMap<Player,
                                        Integer> indexMap, @NotNull Player player) {

        List<String> tabList = getMessageMap(map, player);

        Integer index = indexMap.get(player);
        if (index == null) index = 0;

        boolean isRandom = config.getVaultBoolean(player, this + ".random");

        if (isRandom) {
            index = RandomUtil.nextInt(0, tabList.size());
        } else {
            index++;
            index = index % tabList.size();
        }

        indexMap.put(player, index);

        return MessageUtil.formatAll(player, tabList.get(index));
    }

    private List<String> getMessageMap(HashMap<String, List<String>> map, Player player) {
        String playerGroup = PlayerUtil.getPrimaryGroup(player);
        List<String> messageList = map.get(playerGroup);
        if (messageList != null) return messageList;

        messageList = new ArrayList<>();

        List<String> tempList = locale.getVaultStringList(player, this + "." + ".message");

        StringBuilder stringBuilder = new StringBuilder();

        for (String tempString : tempList) {
            if (tempString.equals("<next>")) {
                messageList.add(stringBuilder.substring(0, stringBuilder.length() - 1));
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(tempString).append("\n");
            }
        }

        messageList.add(stringBuilder.substring(0, stringBuilder.length() - 1));

        map.put(playerGroup, messageList);
        return messageList;
    }
}
