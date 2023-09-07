package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.HookManager;
import net.flectone.misc.advancement.FAdvancement;
import net.flectone.misc.advancement.FAdvancementType;
import net.flectone.misc.commands.FCommand;
import net.flectone.misc.components.FAdvancementComponent;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

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
        boolean isEnable = config.getBoolean("advancement.message.enable");
        if (isEnable) register();
        else unregister();

        Bukkit.getWorlds().forEach(world -> setAnnounceAdvancements(world, !isEnable));
    }

    private static void setAnnounceAdvancements(@NotNull World world, boolean enable) {
        Object value = world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS);
        if (value == null || (boolean) value == enable) return;

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, enable);
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
                || !config.getBoolean("advancement.message." + fAdvancementType + ".visible"))
            return;

        String configMessage = locale.getString("advancement." + fAdvancementType + ".name");

        String formatMessage = ObjectUtil.formatString(configMessage, null)
                .replace("<player>", player.getName())
                .replace("<advancement>", fAdvancement.getTitle());

        ArrayList<String> placeholders = new ArrayList<>(List.of("<player>", "<advancement>"));

        if (HookManager.enabledDiscordSRV) {
            FDiscordSRV.sendAdvancementMessage(player, fAdvancement, formatMessage);
        }

        FCommand fCommand = new FCommand(player, "advancement", "death", formatMessage.split(" "));
        fCommand.sendConsoleMessage(formatMessage);

        fCommand.getFilteredListRecipient().parallelStream()
                .forEach(recipient -> {
                    String string = ObjectUtil.formatString(configMessage, recipient, player);
                    ArrayList<String> finalPlaceholders = ObjectUtil.splitLine(string, placeholders);
                    recipient.spigot().sendMessage(new FAdvancementComponent(finalPlaceholders, recipient, player, fAdvancement).get());
                });


    }
}
