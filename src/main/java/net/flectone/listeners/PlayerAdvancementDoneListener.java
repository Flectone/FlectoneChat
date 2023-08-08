package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.advancement.FAdvancement;
import net.flectone.misc.advancement.FAdvancementType;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerAdvancementDoneListener implements Listener {

    private static PlayerAdvancementDoneListener instance;

    public PlayerAdvancementDoneListener() {
        instance = this;
    }

    public static void unregister() {
        if (instance == null) return;
        PlayerAdvancementDoneEvent.getHandlerList().unregister(instance);
    }

    public static void register() {
        if (instance != null) return;
        Bukkit.getPluginManager().registerEvents(new PlayerAdvancementDoneListener(), Main.getInstance());
    }

    public static void reload() {
        boolean isEnable = Main.config.getBoolean("advancement.message.enable");
        if (isEnable) register();
        else unregister();

        Bukkit.getWorlds().forEach(world -> setVisibleDefaultAnnounce(world, !isEnable));
    }

    private static void setVisibleDefaultAnnounce(@NotNull World world, boolean visible) {
        Object value = world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
        if (value == null) return;

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, visible);
    }

    @EventHandler
    public void onPlayerAdvancementDone(@NotNull PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipe/") || key.contains("recipes/")) return;

        Player player = event.getPlayer();

        if (FSuperVanish.isVanished(player)) return;

        FAdvancement fAdvancement = new FAdvancement(event.getAdvancement());
        FAdvancementType fAdvancementType = fAdvancement.getType();

        if (fAdvancementType == FAdvancementType.UNKNOWN
                || fAdvancement.isHidden()
                || !fAdvancement.announceToChat()
                || !Main.config.getBoolean("advancement.message." + fAdvancementType + ".visible"))
            return;

        String formatMessage = Main.locale.getString("advancement." + fAdvancementType + ".name");
        ArrayList<String> placeholders = new ArrayList<>(List.of("<player>", "<advancement>"));

        FDiscordSRV.sendAdvancementMessage(player, fAdvancement, formatMessage);

        Bukkit.getOnlinePlayers().parallelStream()
                .filter(recipient -> {
                    FPlayer fPlayer = FPlayerManager.getPlayer(recipient);

                    return fPlayer != null && !fPlayer.isIgnored(player);
                })
                .forEach(recipient -> {
                    String string = ObjectUtil.formatString(formatMessage, recipient, player);
                    ArrayList<String> finalPlaceholders = ObjectUtil.splitLine(string, placeholders);
                    recipient.spigot().sendMessage(createAdvancementComponent(finalPlaceholders, recipient, player, fAdvancement));
                });
    }

    private BaseComponent[] createAdvancementComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FAdvancement fAdvancement) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();
        for (String mainPlaceholder : placeholders) {

            switch (mainPlaceholder) {
                case "<player>" -> mainBuilder.append(createClickableComponent(mainColor, sender, recipient));
                case "<advancement>" -> {
                    TranslatableComponent translatableComponent = new TranslatableComponent(fAdvancement.getTranslateKey());
                    String hover = Main.locale.getFormatString("advancement." + fAdvancement.getType() + ".hover", recipient, sender);
                    String hoverColor = "";
                    ComponentBuilder hoverBuilder = new ComponentBuilder();
                    for (String hoverPlaceholder : ObjectUtil.splitLine(hover, new ArrayList<>(List.of("<name>", "<description>")))) {
                        switch (hoverPlaceholder) {
                            case "<name>" -> {
                                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
                                hoverBuilder.append(new TranslatableComponent(fAdvancement.getTranslateKey()));
                                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
                            }
                            case "<description>" -> {
                                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
                                hoverBuilder.append(new TranslatableComponent(fAdvancement.getTranslateDesc()));
                                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
                            }
                            default ->
                                    hoverBuilder.append(TextComponent.fromLegacyText(hoverColor + hoverPlaceholder), ComponentBuilder.FormatRetention.NONE);
                        }

                        hoverColor = ChatColor.getLastColors(hoverColor + hoverBuilder.getCurrentComponent().toString());
                    }
                    translatableComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
                    mainBuilder.append(TextComponent.fromLegacyText(mainColor));
                    mainBuilder.append(translatableComponent);
                    mainBuilder.append(TextComponent.fromLegacyText(mainColor));
                }
                default -> mainBuilder.append(TextComponent.fromLegacyText(mainColor + mainPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = ChatColor.getLastColors(mainColor + mainBuilder.getCurrentComponent().toString());

        }

        return mainBuilder.create();
    }

    private TextComponent createClickableComponent(@NotNull String chatColor, @NotNull CommandSender sender, @NotNull CommandSender recipient) {
        String playerName = sender.getName();
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("player.hover-message", recipient, sender)
                .replace("<player>", playerName);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + playerName));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
    }
}
