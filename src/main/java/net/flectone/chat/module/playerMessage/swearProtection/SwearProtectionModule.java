package net.flectone.chat.module.playerMessage.swearProtection;

import net.flectone.chat.module.FModule;
import net.flectone.chat.util.SwearUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

import static net.flectone.chat.manager.FileManager.config;

public class SwearProtectionModule extends FModule {

    public SwearProtectionModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    public String replace(@Nullable Player player, @NotNull String string) {
        if (hasNoPermission(player)) return string;
        if (!hasNoPermission(player, "bypass")) return string;

        String[] words = string.split(" ");
        String swearHideSymbol = config.getVaultString(player, this + ".symbol");

        StringBuilder stringBuilder = new StringBuilder();

        int lastX = 0;

        for (int x = 0; x < words.length; x++) {
            String word = words[x];

            stringBuilder.append(word);
            if (SwearUtil.contains(stringBuilder.toString())) {
                String textWithSwear = stringBuilder.toString();

                boolean remove = false;
                for (int y = lastX; y < x; y++) {
                    if (remove) {
                        words[y] = "";
                        continue;
                    }

                    textWithSwear = textWithSwear.substring(words[y].length());
                    if (!SwearUtil.contains(textWithSwear)) {
                        words[y] = "";
                        remove = true;
                    }
                }

                words[x] = swearHideSymbol.repeat(3);

                stringBuilder = new StringBuilder();
                lastX = x + 1;
            }
        }

        return Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .collect(Collectors.joining(" "));
    }
}