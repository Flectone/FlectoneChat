package net.flectone.chat.module.playerMessage.sign;

import net.flectone.chat.builder.MessageBuilder;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Moderation;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.commands.SpyListener;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class SignListener extends FListener {
    public SignListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        register();
    }

    @EventHandler
    public void signChangeEvent(@NotNull SignChangeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;

        FPlayer fPlayer = FPlayerManager.get(player);

        if (fPlayer != null && fPlayer.isMuted()) {
            String message = locale.getVaultString(fPlayer.getPlayer(), "commands.muted");

            Moderation mute = fPlayer.getMute();
            message = message
                    .replace("<time>", TimeUtil.convertTime(fPlayer.getPlayer(), mute.getTime() - TimeUtil.getCurrentTime()))
                    .replace("<reason>", mute.getReason())
                    .replace("<moderator>", mute.getModeratorName());

            message = MessageUtil.formatAll(fPlayer.getPlayer(), message);

            player.sendMessage(message);

            event.setCancelled(true);
        }

        List<String> features = config.getVaultStringList(player, getModule() + ".features");
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        for (int x = 0; x < event.getLines().length; x++) {
            String string = event.getLine(x);

            if (string == null || string.isEmpty()) continue;


            MessageBuilder messageBuilder = new MessageBuilder(player, itemInHand, string, features);
            event.setLine(x, messageBuilder.getMessage(""));

            SpyListener.send(player, "sign", messageBuilder.getMessage(""));
        }
    }
}
