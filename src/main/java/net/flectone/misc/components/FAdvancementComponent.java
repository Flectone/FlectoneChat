package net.flectone.misc.components;

import net.flectone.misc.advancement.FAdvancement;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FAdvancementComponent extends FComponent{

    public FAdvancementComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FAdvancement fAdvancement) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();
        for (String mainPlaceholder : placeholders) {

            switch (mainPlaceholder) {
                case "<player>" -> mainBuilder.append(new FPlayerComponent(recipient, sender, mainColor + sender.getName()).get());
                case "<advancement>" -> {
                    FComponent advancementComponent = new FLocaleComponent(fAdvancement.getTranslateKey());

                    String hover = locale.getFormatString("advancement." + fAdvancement.getType() + ".hover", recipient, sender);
                    String hoverColor = "";
                    ComponentBuilder hoverBuilder = new ComponentBuilder();
                    for (String hoverPlaceholder : ObjectUtil.splitLine(hover, new ArrayList<>(List.of("<name>", "<description>")))) {
                        switch (hoverPlaceholder) {
                            case "<name>" -> hoverBuilder
                                    .append(new FColorComponent(new FLocaleComponent(fAdvancement.getTranslateKey()), hoverColor).get());

                            case "<description>" -> hoverBuilder
                                    .append(new FColorComponent(new FLocaleComponent(fAdvancement.getTranslateDesc()), hoverColor).get());

                            default -> hoverBuilder
                                    .append(new FComponent(hoverColor + hoverPlaceholder).get(), ComponentBuilder.FormatRetention.NONE);
                        }

                        hoverColor = getLastColor(hoverColor, hoverBuilder);
                    }

                    advancementComponent.addHoverText(hoverBuilder.create());

                    mainBuilder.append(new FColorComponent(advancementComponent, mainColor).get());
                }
                default -> mainBuilder
                        .append(new FComponent(mainColor + mainPlaceholder).get(), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = getLastColor(mainColor, mainBuilder);
        }

        set(mainBuilder.create());
    }
}
