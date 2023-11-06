package net.flectone.chat.module.playerMessage.patterns;

import net.flectone.chat.module.FModule;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.flectone.chat.manager.FileManager.config;

public class PatternsModule extends FModule {

    private static HashMap<String, HashMap<String, String>> PATTERN_MAP = new HashMap<>();

    public PatternsModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @NotNull
    public HashMap<String, String> load(@Nullable Player player) {
        HashMap<String, String> groupPatternMap = new HashMap<>();

        String vaultGroup = PlayerUtil.getPrimaryGroup(player);
        if (PATTERN_MAP.containsKey(vaultGroup)) return PATTERN_MAP.get(vaultGroup);

        List<String> patternList = config.getCustomList(player, this + ".list");

        patternList.forEach(pattern ->
                groupPatternMap.put(pattern, config.getVaultString(player, this + ".list." + pattern)));

        PATTERN_MAP.put(vaultGroup, groupPatternMap);

        return groupPatternMap;
    }

    @NotNull
    public String replace(@Nullable Player player, @NotNull String string) {
        if (!isEnabledFor(player)) return string;
        if (hasNoPermission(player)) return string;

        HashMap<String, String> patterns = load(player);

        for (Map.Entry<String, String> pattern : patterns.entrySet()) {
            string = string.replace(pattern.getKey(), pattern.getValue());
        }

        return string;
    }

}
