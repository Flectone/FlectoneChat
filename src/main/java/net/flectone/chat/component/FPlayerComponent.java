package net.flectone.chat.component;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.player.hover.HoverModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FPlayerComponent extends FComponent {

    public FPlayerComponent(@Nullable CommandSender sender, @Nullable CommandSender recipient, @NotNull String text) {
        super(text);

        if (!(sender instanceof Player player) || !(recipient instanceof Player recip)) return;

        if (config.getVaultBoolean(player, "player.name.hide-invisible")
                && player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && player.hasPermission("flectonechat.player.name.invisible")) {
            text = text.replace(player.getName(), locale.getVaultString(player, "player.name.invisible"));
            set(text);
            return;
        }

        Pair<String, Pair<String, HoverModule.CommandType>> hoverInfo = null;

        FModule fModule = FlectoneChat.getPlugin().getModuleManager().get(HoverModule.class);
        if (fModule instanceof HoverModule hoverModule) {
            hoverInfo = hoverModule.get(player);
        }

        if (hoverInfo == null) return;

        addHoverText(MessageUtil.formatAll(player, recip, hoverInfo.getKey()));

        switch (hoverInfo.getValue().getValue()) {
            case RUN -> addRunCommand(hoverInfo.getValue().getKey());
            case SUGGEST -> addSuggestCommand(hoverInfo.getValue().getKey());
        }
    }
}
