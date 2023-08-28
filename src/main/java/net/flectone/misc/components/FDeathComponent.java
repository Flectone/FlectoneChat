package net.flectone.misc.components;

import net.flectone.misc.entity.player.PlayerDamager;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FDeathComponent extends FComponent {

    public FDeathComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull PlayerDamager playerDamager) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for (String mainPlaceholder : placeholders) {
            switch (mainPlaceholder) {

                case "<player>" -> mainBuilder.append(new FPlayerComponent(recipient, sender, mainColor + sender.getName()).get());

                case "<projectile>", "<killer>" -> {
                    if (playerDamager.getFinalEntity() == null) break;
                    Entity entity = playerDamager.getFinalEntity();
                    if (entity instanceof Player) {
                        mainBuilder.append(new FPlayerComponent(recipient, entity, mainColor + entity.getName()).get());
                        break;
                    }
                    mainBuilder.append(new FColorComponent(new FEntityComponent(recipient, sender, entity), mainColor).get());
                }
                case "<block>" -> {
                    if (!playerDamager.isFinalBlock()) break;
                    mainBuilder.append(new FColorComponent(new FLocaleComponent(playerDamager), mainColor).get());
                }
                case "<due_to>" -> {
                    if (playerDamager.getKiller() == null || playerDamager.getKiller().equals(playerDamager.getFinalEntity())
                            || (playerDamager.getFinalEntity() != null && playerDamager.getKiller().getType().equals(playerDamager.getFinalEntity().getType()))) {
                        break;
                    }
                    String formatDueToMessage = locale.getFormatString("death.due-to", recipient, sender);
                    String dueToColor = "";
                    ComponentBuilder dueToBuilder = new ComponentBuilder();
                    for (String dueToPlaceholder : ObjectUtil.splitLine(formatDueToMessage, new ArrayList<>(List.of("<killer>")))) {
                        if (dueToPlaceholder.equals("<killer>")) {
                            Entity killer = playerDamager.getKiller();
                            if (killer instanceof Player) {
                                dueToBuilder.append(new FPlayerComponent(recipient, killer, dueToColor + killer.getName()).get(), ComponentBuilder.FormatRetention.NONE);

                            } else
                                dueToBuilder.append(new FColorComponent(new FEntityComponent(recipient, sender, killer), dueToColor).get());

                        } else
                            dueToBuilder.append(FComponent.fromLegacyText(dueToColor + dueToPlaceholder), ComponentBuilder.FormatRetention.NONE);

                        dueToColor = getLastColor(dueToColor, dueToBuilder);
                    }
                    mainBuilder.append(dueToBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                }

                case "<by_item>" -> {
                    if (playerDamager.getKillerItemName() == null) break;
                    String formatMessage = locale.getFormatString("death.by-item", recipient, sender);
                    String byItemColor = "";

                    ComponentBuilder byItemBuilder = new ComponentBuilder();
                    for (String byItemPlaceholder : ObjectUtil.splitLine(formatMessage, new ArrayList<>(List.of("<item>")))) {
                        if (byItemPlaceholder.equals("<item>")) {

                            FComponent byItemComponent = new FLocaleComponent(playerDamager.getKillerItem());

                            byItemBuilder.append(new FColorComponent(byItemComponent, byItemColor).get());

                        } else byItemBuilder.append(FComponent.fromLegacyText(byItemColor + byItemPlaceholder));

                        byItemColor = getLastColor(byItemColor, byItemBuilder);

                    }

                    mainBuilder.append(byItemBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                }

                default ->
                        mainBuilder.append(FComponent.fromLegacyText(mainColor + mainPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = getLastColor(mainColor, mainBuilder);
        }

        set(mainBuilder.create());
    }
}
