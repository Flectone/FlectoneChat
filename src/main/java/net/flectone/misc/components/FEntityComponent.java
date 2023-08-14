package net.flectone.misc.components;

import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FEntityComponent extends FComponent{


    public FEntityComponent(@NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull Entity entity) {
        super(new FLocaleComponent(entity));

        String formatHoverMessage = locale.getFormatString("entity.hover-message", recipient, sender)
                .replace("<uuid>", entity.getUniqueId().toString());

        ComponentBuilder hoverBuilder = new ComponentBuilder();

        String hoverColor = "";

        for (String hoverPlaceholder : ObjectUtil.splitLine(formatHoverMessage, new ArrayList<>(List.of("<name>", "<type>")))) {
            switch (hoverPlaceholder) {
                case "<name>" ->
                        hoverBuilder.append(new FColorComponent(new FLocaleComponent(NMSUtil.getMinecraftName(entity)), hoverColor).get());
                case "<type>" ->
                        hoverBuilder.append(new FColorComponent(new FLocaleComponent(NMSUtil.getMinecraftType(entity)), hoverColor).get());
                default ->
                        hoverBuilder.append(fromLegacyText(hoverColor + hoverPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            hoverColor = getLastColor(hoverColor, hoverBuilder);
        }

        addHoverText(hoverBuilder.create());
    }

}
