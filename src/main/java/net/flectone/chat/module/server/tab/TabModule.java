package net.flectone.chat.module.server.tab;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class TabModule extends FModule {

    private static final HashMap<Player, Integer> HEADER_INDEX_MAP = new HashMap<>();
    private static final HashMap<Player, Integer> FOOTER_INDEX_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> TAB_HEADER_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> TAB_FOOTER_MAP = new HashMap<>();

    public TabModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new TabListener(this));
        FActionManager.add(new TabTicker(this));
        FActionManager.add(new TabPlayerPingTicker(this));
    }

    public void setPlayerListName(@NotNull Player player, @NotNull String string) {
        player.setPlayerListName(string);
    }

    public void setFooter(@NotNull Player player, @NotNull String string) {
        player.setPlayerListFooter(string);
    }

    public void setHeader(@NotNull Player player, @NotNull String string) {
        player.setPlayerListHeader(string);
    }

    public void update(@NotNull Player player) {
        if (config.getVaultBoolean(player, this + ".header.enable")
                && !hasNoPermission(player, "header")) {

            setHeader(player, incrementIndexAndGet(TAB_HEADER_MAP, HEADER_INDEX_MAP, player, "header"));
        }

        if (config.getVaultBoolean(player, this + ".footer.enable")
                && !hasNoPermission(player, "footer")) {

            setFooter(player, incrementIndexAndGet(TAB_FOOTER_MAP, FOOTER_INDEX_MAP, player, "footer"));
        }

        if (config.getVaultBoolean(player, this + ".player-list-name.enable")
                && !hasNoPermission(player, "player-list-name")) {

            FModule fModule = FlectoneChat.getModuleManager().get(NameModule.class);
            if (fModule instanceof NameModule nameModule) {
                setPlayerListName(player, nameModule.getTab(player));
            }
        }
    }

    private String incrementIndexAndGet(HashMap<String, List<String>> map, HashMap<Player, Integer> indexMap, Player player, String type) {

        List<String> tabList = getTabMap(map, player, type);

        Integer index = indexMap.get(player);
        if (index == null) index = 0;

        index++;
        index = index % tabList.size();
        indexMap.put(player, index);

        return MessageUtil.formatAll(player, tabList.get(index));
    }

    private List<String> getTabMap(HashMap<String, List<String>> map, Player player, String type) {
        String playerGroup = PlayerUtil.getVaultGroup(player);
        List<String> tabList = map.get(playerGroup);
        if (tabList != null) return tabList;

        tabList = new ArrayList<>();

        List<String> tempList = locale.getVaultStringList(player, this + "." + type + ".message");

        StringBuilder stringBuilder = new StringBuilder();

        for (String tempString : tempList) {
            if (tempString.equals("<next>")) {
                tabList.add(stringBuilder.substring(0, stringBuilder.length() - 1));
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(tempString).append("\n");
            }
        }

        tabList.add(stringBuilder.substring(0, stringBuilder.length() - 1));

        map.put(playerGroup, tabList);
        return tabList;
    }
}
