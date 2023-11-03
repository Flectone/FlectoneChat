package net.flectone.chat.module.serverMessage.death;

import net.flectone.chat.builder.FComponentBuilder;
import net.flectone.chat.component.FColorComponent;
import net.flectone.chat.component.FLocaleComponent;
import net.flectone.chat.component.FPlayerComponent;
import net.flectone.chat.manager.FActionManager;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.damager.PlayerDamager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.MessageUtil;
import net.flectone.chat.util.NMSUtil;
import net.flectone.chat.util.PlayerUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.flectone.chat.manager.FileManager.config;
import static net.flectone.chat.manager.FileManager.locale;

public class DeathModule extends FModule {
    public DeathModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        FActionManager.add(new DeathListener(this));
    }

    public void sendAll(@NotNull Player sender, @NotNull PlayerDamager playerDamager, @NotNull String message) {

        PlayerUtil.getPlayersWithFeature(this + ".enable").forEach(player -> {

            FPlayer fPlayer = FPlayerManager.get(player);
            if (fPlayer == null) return;

            String death = fPlayer.getSettings().getValue(Settings.Type.DEATH);
            boolean enabled = death == null || Integer.parseInt(death) != -1;
            if (!enabled) return;

            if (fPlayer.getIgnoreList().contains(sender.getUniqueId())) return;

            FComponentBuilder fComponentBuilder = new FComponentBuilder(message);

            fComponentBuilder.replace("<player>", (componentBuilder, color) ->
                    componentBuilder.append(new FPlayerComponent(sender, player, color + sender.getName()).get()));

            fComponentBuilder.replace("<projectile>", ((componentBuilder, color) ->
                    createEntityComponent(sender, player, playerDamager.getFinalEntity(), componentBuilder, color)));

            fComponentBuilder.replace("<killer>", ((componentBuilder, color) ->
                    createEntityComponent(sender, player, playerDamager.getFinalEntity(), componentBuilder, color)));

            fComponentBuilder.replace("<block>", ((componentBuilder, color) -> {
                if (!playerDamager.isFinalBlock()) return;
                componentBuilder.append(new FColorComponent(new FLocaleComponent(playerDamager), color).get());
            }));

            fComponentBuilder.replace("<due_to>", ((componentBuilder, color) -> {
                if (playerDamager.getKiller() == null
                        || playerDamager.getKiller().equals(playerDamager.getFinalEntity())
                        || (playerDamager.getFinalEntity() != null && playerDamager.getKiller().getType().equals(playerDamager.getFinalEntity().getType()))) {
                    return;
                }

                String formatDueToMessage = locale.getVaultString(player, this + ".due-to");

                FComponentBuilder dueToComponentBuilder = new FComponentBuilder(formatDueToMessage);

                dueToComponentBuilder.replace("<killer>", (dueToBuilder, dueToColor) ->
                        createEntityComponent(sender, player, playerDamager.getKiller(), dueToBuilder, dueToColor));

                componentBuilder.append(dueToComponentBuilder.build(sender, player));
            }));

            fComponentBuilder.replace("<by_item>", (componentBuilder, color) -> {
                if (playerDamager.getKillerItemName() == null) return;

                String formatMessage = locale.getVaultString(sender, this + ".by-item");
                FComponentBuilder byItemComponentBuilder = new FComponentBuilder(formatMessage);

                byItemComponentBuilder.replace("<item>", (byItemBuilder, byItemColor) ->
                        byItemBuilder.append(new FColorComponent(new FLocaleComponent(playerDamager.getKillerItem()), byItemColor).get()));

                componentBuilder.append(byItemComponentBuilder.build(sender, player));
            });

            player.spigot().sendMessage(fComponentBuilder.build(sender, player));
        });
    }

    public void createEntityComponent(@NotNull Player sender, @NotNull Player player, @Nullable Entity entity, @NotNull ComponentBuilder componentBuilder, String color) {
        if (entity == null) return;

        if (entity instanceof Player) {
            componentBuilder.append(new FPlayerComponent(player, entity, color + entity.getName()).get());
            return;
        }

        FLocaleComponent fLocaleComponent = new FLocaleComponent(entity);

        if (config.getVaultBoolean(player, this + ".entity.hover.enable")) {

            String formatHoverMessage = locale.getVaultString(player, this + ".entity.hover.message")
                    .replace("<uuid>", entity.getUniqueId().toString());

            formatHoverMessage = MessageUtil.formatAll(sender, player, formatHoverMessage);

            FComponentBuilder entityHoverComponentBuilder = new FComponentBuilder(formatHoverMessage);

            entityHoverComponentBuilder.replace("<name>", (hoverBuilder, hoverColor) ->
                    hoverBuilder.append(new FColorComponent(new FLocaleComponent(NMSUtil.getMinecraftName(entity)), hoverColor).get()));

            entityHoverComponentBuilder.replace("<type>", (hoverBuilder, hoverColor) ->
                    hoverBuilder.append(new FColorComponent(new FLocaleComponent(NMSUtil.getMinecraftType(entity)), hoverColor).get()));

            fLocaleComponent.addHoverText(entityHoverComponentBuilder.build(sender, player));
        }

        componentBuilder.append(new FColorComponent(fLocaleComponent, color).get());
    }
}
