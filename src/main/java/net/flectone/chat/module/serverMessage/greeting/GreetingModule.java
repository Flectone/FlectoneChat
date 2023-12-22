package net.flectone.chat.module.serverMessage.greeting;

import net.flectone.chat.component.FImageComponent;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreetingModule extends FModule {

    public GreetingModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new GreetingListener(this));
    }

    public void send(@NotNull Player player) {
        if (!isEnabledFor(player)) return;

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return;
        if (fPlayer.getSettings() == null) return;

        String join = fPlayer.getSettings().getValue(Settings.Type.GREETING);
        boolean enabled = join == null || Integer.parseInt(join) != -1;
        if (!enabled) return;

        String imageUrl = PlayerUtil.constructAvatarUrl(player);
        FImageComponent fImageComponent = new FImageComponent(imageUrl);
        String convertedImage = fImageComponent.getConvertedImage().substring(1);

        List<String> messageList = locale.getVaultStringList(player, this + ".message");
        String message = String.join("\n", messageList);

        for (String pixels : convertedImage.split("\n")) {
            message = message.replaceFirst("########", pixels);
        }

        message = MessageUtil.formatAll(player, MessageUtil.formatPlayerString(player, message));
        player.sendMessage(message);

        fPlayer.playSound(player, player, this.toString());
    }
}
