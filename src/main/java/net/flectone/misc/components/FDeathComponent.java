package net.flectone.misc.components;

import net.flectone.misc.entity.FDamager;
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

    public FDeathComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FDamager fDamager) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for (String mainPlaceholder : placeholders) {
            switch (mainPlaceholder) {

                case "<player>" -> mainBuilder.append(new FPlayerComponent(recipient, sender, mainColor + sender.getName()).get());

                case "<projectile>", "<killer>" -> {
                    if (fDamager.getFinalEntity() == null) break;
                    Entity entity = fDamager.getFinalEntity();
                    if (entity instanceof Player) {
                        mainBuilder.append(new FPlayerComponent(recipient, entity, mainColor + entity.getName()).get());
                        break;
                    }
                    mainBuilder.append(new FColorComponent(new FEntityComponent(recipient, sender, entity), mainColor).get());
                }
                case "<block>" -> {
                    if (!fDamager.isFinalBlock()) break;
                    mainBuilder.append(new FColorComponent(new FLocaleComponent(fDamager), mainColor).get());
                }
                case "<due_to>" -> {
                    if (fDamager.getKiller() == null || fDamager.getKiller().equals(fDamager.getFinalEntity())
                            || (fDamager.getFinalEntity() != null && fDamager.getKiller().getType().equals(fDamager.getFinalEntity().getType()))) {
                        break;
                    }
                    String formatDueToMessage = locale.getFormatString("death.due-to", recipient, sender);
                    String dueToColor = "";
                    ComponentBuilder dueToBuilder = new ComponentBuilder();
                    for (String dueToPlaceholder : ObjectUtil.splitLine(formatDueToMessage, new ArrayList<>(List.of("<killer>")))) {
                        if (dueToPlaceholder.equals("<killer>")) {
                            Entity killer = fDamager.getKiller();
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
                    if (fDamager.getKillerItemName() == null) break;
                    String formatMessage = locale.getFormatString("death.by-item", recipient, sender);
                    String byItemColor = "";

                    ComponentBuilder byItemBuilder = new ComponentBuilder();
                    for (String byItemPlaceholder : ObjectUtil.splitLine(formatMessage, new ArrayList<>(List.of("<item>")))) {
                        if (byItemPlaceholder.equals("<item>")) {

                            FComponent byItemComponent = new FLocaleComponent(fDamager.getKillerItem());

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
