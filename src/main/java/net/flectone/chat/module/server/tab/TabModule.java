package net.flectone.chat.module.server.tab;

import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.name.NameModule;
import net.flectone.chat.module.server.tab.playerList.PlayerListModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TabModule extends FModule {

    private final HashMap<Player, Integer> HEADER_INDEX_MAP = new HashMap<>();
    private final HashMap<Player, Integer> FOOTER_INDEX_MAP = new HashMap<>();
    private final HashMap<String, List<String>> TAB_HEADER_MAP = new HashMap<>();
    private final HashMap<String, List<String>> TAB_FOOTER_MAP = new HashMap<>();

    public TabModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        new PlayerListModule(this, "player-list");

        actionManager.add(new TabListener(this));

        if (config.getBoolean("default." + this + ".update.enable")) {
            actionManager.add(new TabTicker(this));
        }
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

            FModule fModule = moduleManager.get(NameModule.class);
            if (fModule instanceof NameModule nameModule) {
                setPlayerListName(player, nameModule.getTab(player));
            }
        }
    }

    private String incrementIndexAndGet(HashMap<String, List<String>> map, HashMap<Player, Integer> indexMap, Player player, String type) {

        List<String> tabList = getTabMap(map, player, type);

        if (tabList.isEmpty()) return "";

        Integer index = indexMap.get(player);
        if (index == null) index = 0;

        index++;
        index = index % tabList.size();
        indexMap.put(player, index);

        return MessageUtil.formatAll(player, tabList.get(index));
    }

    private List<String> getTabMap(HashMap<String, List<String>> map, Player player, String type) {
        String playerGroup = PlayerUtil.getPrimaryGroup(player);
        List<String> tabList = map.get(playerGroup);
        if (tabList != null) return tabList;

        tabList = new ArrayList<>();

        List<String> tempList = locale.getVaultStringList(player, this + "." + type + ".message");

        if (tempList.isEmpty()) {
            return tabList;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (String tempString : tempList) {
            if (tempString.equals("<next>")) {
                tabList.add(stringBuilder.substring(0, stringBuilder.length() - 1));
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(tempString).append("\n");
            }
        }

        tabList.add(!stringBuilder.isEmpty() ? stringBuilder.substring(0, stringBuilder.length() - 1) : stringBuilder.toString());

        map.put(playerGroup, tabList);
        return tabList;
    }
}
