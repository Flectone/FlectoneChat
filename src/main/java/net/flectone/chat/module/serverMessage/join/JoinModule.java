package net.flectone.chat.module.serverMessage.join;

import net.flectone.chat.builder.FComponentBuilder;
import net.flectone.chat.component.FPlayerComponent;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JoinModule extends FModule {
    public JoinModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new JoinListener(this));
    }

    public void sendAll(@NotNull Player sender, @NotNull String message) {
        PlayerUtil.getPlayersWithFeature(this + ".enable").forEach(player -> {

            FPlayer fPlayer = FPlayerManager.get(player);
            if (fPlayer == null) return;

            String join = fPlayer.getSettings().getValue(Settings.Type.JOIN);
            boolean enabled = join == null || Integer.parseInt(join) != -1;
            if (!enabled) return;

            if (fPlayer.getIgnoreList().contains(sender.getUniqueId())) return;

            FComponentBuilder fComponentBuilder = new FComponentBuilder(message);

            fComponentBuilder.replace("<player>", (componentBuilder, color) ->
                    componentBuilder.append(new FPlayerComponent(sender, player, color + sender.getName()).get()));

            player.spigot().sendMessage(fComponentBuilder.build(sender, player));
        });
    }
}
