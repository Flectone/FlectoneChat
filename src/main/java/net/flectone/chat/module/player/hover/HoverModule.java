package net.flectone.chat.module.player.hover;


import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.Pair;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverModule extends FModule {

    public HoverModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();
    }

    @Nullable
    public Pair<String, Pair<String, CommandType>> get(@NotNull Player player) {
        if (!isEnabledFor(player)) return null;

        String hoverText = locale.getVaultString(player, this + ".message");
        hoverText = MessageUtil.formatPlayerString(player, hoverText);

        String command = config.getVaultString(player, this + ".command")
                .replace("<player>", player.getName());

        CommandType commandType = CommandType.fromString(config.getVaultString(player, this + ".command-type"));

        return new Pair<>(hoverText, new Pair<>(command, commandType));
    }

    public enum CommandType {

        SUGGEST("suggest"),
        RUN("run");

        CommandType(String name) {}

        public static CommandType fromString(String string) {
            if (string.equalsIgnoreCase("run")) return RUN;
            return SUGGEST;
        }
    }
}
