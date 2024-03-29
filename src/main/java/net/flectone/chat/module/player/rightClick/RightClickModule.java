package net.flectone.chat.module.player.rightClick;

import net.flectone.chat.component.FComponent;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RightClickModule extends FModule {

    public RightClickModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new RightClickListener(this));
    }

    public void sendAction(@NotNull Player player, @NotNull String string) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, FComponent.fromLegacyText(string));

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return;
        fPlayer.playSound(player, player, this.toString());
    }
}
