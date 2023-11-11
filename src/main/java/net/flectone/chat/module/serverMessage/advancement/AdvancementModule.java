package net.flectone.chat.module.serverMessage.advancement;

import net.flectone.chat.builder.FComponentBuilder;
import net.flectone.chat.component.FColorComponent;
import net.flectone.chat.component.FComponent;
import net.flectone.chat.component.FLocaleComponent;
import net.flectone.chat.component.FPlayerComponent;
import net.flectone.chat.model.advancement.FAdvancement;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.model.player.Settings;
import net.flectone.chat.module.FModule;
import net.flectone.chat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class AdvancementModule extends FModule {

    private final HashMap<World, Object> LAST_WORLD_ANNOUNCE_ADVANCEMENT_MAP = new HashMap<>();
    public AdvancementModule(FModule module, String name) {
        super(module, name);
        init();
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        register();

        actionManager.add(new AdvancementListener(this));

        initAnnounce();
    }

    public void initAnnounce() {
        Bukkit.getWorlds().forEach(world -> {
            Object value = world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
            if (value == null) return;

            LAST_WORLD_ANNOUNCE_ADVANCEMENT_MAP.put(world, value);
            setAnnounce(world, false);
        });
    }

    public void terminateAnnounce() {
        Bukkit.getWorlds().forEach(world -> {
            Object value = LAST_WORLD_ANNOUNCE_ADVANCEMENT_MAP.get(world);
            if (value == null) return;

            setAnnounce(world, (boolean) value);
        });
    }

    private void setAnnounce(@NotNull World world, boolean announce) {
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, announce);
    }

    public void sendAll(@NotNull Player sender, @NotNull FAdvancement fAdvancement, @NotNull String message) {
        PlayerUtil.getPlayersWithFeature(this + ".enable")
                .stream()
                .filter(this::isEnabledFor)
                .forEach(player -> {
                    FPlayer fPlayer = playerManager.get(player);
                    if (fPlayer == null) return;

                    String advancement = fPlayer.getSettings().getValue(Settings.Type.ADVANCEMENT);
                    boolean enabled = advancement == null || Integer.parseInt(advancement) != -1;
                    if (!enabled) return;

                    if (fPlayer.getIgnoreList().contains(sender.getUniqueId())) return;

                    sendMessage(sender, player, fAdvancement, message);

                    fPlayer.playSound(sender, player, this.toString());
                });

        sendMessage(sender, null, fAdvancement, message);
    }

    public void sendMessage(@NotNull Player sender, @Nullable Player player, @NotNull FAdvancement fAdvancement, @NotNull String message) {
        FComponentBuilder fComponentBuilder = new FComponentBuilder(message);

        fComponentBuilder.replace("<player>", (componentBuilder, color) ->
                componentBuilder.append(new FPlayerComponent(sender, player, color + sender.getName()).get()));

        fComponentBuilder.replace("<advancement>", ((componentBuilder, color) -> {
            FComponent advancementComponent = new FLocaleComponent(fAdvancement.getTranslateKey());

            String hover = locale.getVaultString(sender, this + "." + fAdvancement.getType() + ".hover");
            FComponentBuilder hoverComponentBuilder = new FComponentBuilder(hover);
            hoverComponentBuilder.replace("<name>", (hoverBuilder, hoverColor) ->
                    hoverBuilder.append(new FColorComponent(new FLocaleComponent(fAdvancement.getTranslateKey()), hoverColor).get()));
            hoverComponentBuilder.replace("<description>", (hoverBuilder, hoverColor) ->
                    hoverBuilder.append(new FColorComponent(new FLocaleComponent(fAdvancement.getTranslateDesc()), hoverColor).get()));

            advancementComponent.addHoverText(hoverComponentBuilder.build(sender, player));

            componentBuilder.append(advancementComponent.get());
        }));

        if (player != null) {
            player.spigot().sendMessage(fComponentBuilder.build(sender, player));
        } else {
            FPlayer.sendToConsole(fComponentBuilder.build(sender, null));
        }
    }
}
