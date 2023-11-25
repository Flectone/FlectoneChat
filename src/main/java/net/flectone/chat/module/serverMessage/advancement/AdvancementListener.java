package net.flectone.chat.module.serverMessage.advancement;

import net.flectone.chat.model.advancement.FAdvancement;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

public class AdvancementListener extends FListener {

    public AdvancementListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void playerAdvancementEvent(@NotNull PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipe/") || key.contains("recipes/")) return;

        Player player = event.getPlayer();
        if (hasNoPermission(player)) return;
        if (IntegrationsModule.isVanished(player)) return;

        FAdvancement fAdvancement = new FAdvancement(event.getAdvancement());
        FAdvancement.Type fAdvancementType = fAdvancement.getType();

        if (fAdvancementType == FAdvancement.Type.UNKNOWN
                || fAdvancement.isHidden()
                || !fAdvancement.announceToChat()
                || !config.getVaultBoolean(player, getModule() + "." + fAdvancementType + ".visible"))
            return;

        String configMessage = locale.getVaultString(player, getModule() + "." + fAdvancementType + ".name");

        ((AdvancementModule) getModule()).sendAll(player, fAdvancement, configMessage);

        IntegrationsModule.sendDiscordAdvancement(player, fAdvancement);
    }
}
