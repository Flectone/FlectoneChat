package net.flectone.misc.components;

import net.flectone.misc.advancement.FAdvancement;
import net.flectone.misc.entity.FDamager;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.managers.FileManager.locale;

public class FComponent {

    private final BaseComponent component;

    public FComponent(String text) {
        this.component = componentFromLegacyText(text);
    }

    public FComponent() {
        this.component = new TextComponent();
    }

    public FComponent(BaseComponent baseComponent) {
        this.component = baseComponent;
    }

    public static FComponent createVote(String voteType, String voteId) {
        String agreeString = locale.getFormatString("command.poll.format." + voteType, null);

        return new FComponent(agreeString)
                .addRunCommand("/poll vote " + voteId + " " + voteType);
    }

    public static FComponent createPlayer(CommandSender recipient, CommandSender sender, String text) {
        String playerName = sender.getName();

        String showText = locale.getFormatString("player.hover-message", recipient, sender)
                .replace("<player>", playerName);

        return new FComponent(text)
                .addSuggestCommand("/msg " + playerName + " ")
                .addHoverText(showText);
    }

    public static FComponent createURL(CommandSender recipient, CommandSender sender, String text, String url) {
        String showText = locale.getFormatString("chat.url.hover-message", recipient, sender);

        return new FComponent(text)
                .addOpenURL(url)
                .addHoverText(showText);
    }

    public static BaseComponent[] addColor(BaseComponent translatableComponent, String color) {
        BaseComponent[] colorComponents = fromLegacyText(color);
        return new ComponentBuilder()
                .append(colorComponents)
                .append(translatableComponent)
                .append(colorComponents)
                .create();
    }

    public static FComponent createTranslatableEntity(@NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull Entity entity) {
        FComponent component = new FComponent(new TranslatableComponent(NMSUtil.getMinecraftName(entity)));

        String formatHoverMessage = locale.getFormatString("entity.hover-message", recipient, sender)
                .replace("<uuid>", entity.getUniqueId().toString());

        ComponentBuilder hoverBuilder = new ComponentBuilder();

        String hoverColor = "";

        for (String hoverPlaceholder : ObjectUtil.splitLine(formatHoverMessage, new ArrayList<>(List.of("<name>", "<type>")))) {
            switch (hoverPlaceholder) {
                case "<name>" ->
                        hoverBuilder.append(addColor(new TranslatableComponent(NMSUtil.getMinecraftName(entity)), hoverColor));
                case "<type>" ->
                        hoverBuilder.append(addColor(new TranslatableComponent(NMSUtil.getMinecraftType(entity)), hoverColor));
                default ->
                        hoverBuilder.append(fromLegacyText(hoverColor + hoverPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            hoverColor = getLastColor(hoverColor, hoverBuilder);
        }

        return component.addHoverText(hoverBuilder.create());
    }

    private static BaseComponent[] fromLegacyText(String text) {
        return TextComponent.fromLegacyText(text);
    }

    public static BaseComponent[] createDeathComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FDamager fDamager) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for (String mainPlaceholder : placeholders) {
            switch (mainPlaceholder) {

                case "<player>" -> mainBuilder.append(createPlayer(recipient, sender, mainColor + sender.getName()).get());

                case "<projectile>", "<killer>" -> {
                    if (fDamager.getFinalEntity() == null) break;
                    Entity entity = fDamager.getFinalEntity();
                    if (entity instanceof Player) {
                        mainBuilder.append(createPlayer(recipient, sender, mainColor + sender.getName()).get());
                        break;
                    }
                    mainBuilder.append(addColor(createTranslatableEntity(recipient, sender, entity).get(), mainColor));
                }
                case "<block>" -> {
                    if (!fDamager.isFinalBlock()) break;
                    mainBuilder.append(addColor(new TranslatableComponent(fDamager.getDamagerTranslateName()), mainColor));
                }
                case "<due_to>" -> {
                    if (fDamager.getKiller() == null || fDamager.getKiller().equals(fDamager.getFinalEntity())
                            || fDamager.getFinalEntity() != null && fDamager.getKiller().getType().equals(fDamager.getFinalEntity().getType())) {
                        break;
                    }
                    String formatDueToMessage = locale.getFormatString("death.due-to", recipient, sender);
                    String dueToColor = "";
                    ComponentBuilder dueToBuilder = new ComponentBuilder();
                    for (String dueToPlaceholder : ObjectUtil.splitLine(formatDueToMessage, new ArrayList<>(List.of("<killer>")))) {
                        if (dueToPlaceholder.equals("<killer>")) {
                            Entity killer = fDamager.getKiller();
                            if (killer instanceof Player) {
                                dueToBuilder.append(createPlayer(recipient, killer, dueToColor + killer.getName()).get(), ComponentBuilder.FormatRetention.NONE);

                            } else
                                dueToBuilder.append(addColor(createTranslatableEntity(recipient, sender, killer).get(), dueToColor));

                        } else
                            dueToBuilder.append(fromLegacyText(dueToColor + dueToPlaceholder), ComponentBuilder.FormatRetention.NONE);

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

                            FComponent byItemComponent = new FComponent(new TranslatableComponent(fDamager.getKillerItemName()))
                                    .addHoverItem(fDamager.getKillerItemAsJson());

                            byItemBuilder.append(addColor(byItemComponent.get(), byItemColor));

                        } else byItemBuilder.append(fromLegacyText(byItemColor + byItemPlaceholder));

                        byItemColor = getLastColor(byItemColor, byItemBuilder);

                    }

                    mainBuilder.append(byItemBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                }

                default ->
                        mainBuilder.append(fromLegacyText(mainColor + mainPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = getLastColor(mainColor, mainBuilder);
        }

        return mainBuilder.create();
    }

    public static BaseComponent[] createAdvancementComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FAdvancement fAdvancement) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();
        for (String mainPlaceholder : placeholders) {

            switch (mainPlaceholder) {
                case "<player>" -> mainBuilder.append(createPlayer(recipient, sender, mainColor + sender.getName()).get());
                case "<advancement>" -> {
                    FComponent translatableComponent = new FComponent(new TranslatableComponent(fAdvancement.getTranslateKey()));

                    String hover = locale.getFormatString("advancement." + fAdvancement.getType() + ".hover", recipient, sender);
                    String hoverColor = "";
                    ComponentBuilder hoverBuilder = new ComponentBuilder();
                    for (String hoverPlaceholder : ObjectUtil.splitLine(hover, new ArrayList<>(List.of("<name>", "<description>")))) {
                        switch (hoverPlaceholder) {
                            case "<name>" -> hoverBuilder
                                    .append(addColor(new TranslatableComponent(fAdvancement.getTranslateKey()), hoverColor));

                            case "<description>" -> hoverBuilder
                                    .append(addColor(new TranslatableComponent(fAdvancement.getTranslateDesc()), hoverColor));

                            default -> hoverBuilder
                                    .append(new FComponent(hoverColor + hoverPlaceholder).get(), ComponentBuilder.FormatRetention.NONE);
                        }

                        hoverColor = getLastColor(hoverColor, hoverBuilder);
                    }

                    translatableComponent.addHoverText(hoverBuilder.create());

                    mainBuilder.append(addColor(translatableComponent.get(), mainColor));
                }
                default -> mainBuilder
                        .append(new FComponent(mainColor + mainPlaceholder).get(), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = getLastColor(mainColor, mainBuilder);
        }
        return mainBuilder.create();
    }

    private static String getLastColor(String lastColor, ComponentBuilder componentBuilder) {
        return ChatColor.getLastColors(lastColor + componentBuilder.getCurrentComponent().toString());
    }

    public static BaseComponent[] createListComponents(String commandName, CommandSender commandSender, int page, int lastPage) {

        String pageLine = locale.getFormatString("command." + commandName + ".page-line", commandSender)
                .replace("<page>", String.valueOf(page))
                .replace("<last-page>", String.valueOf(lastPage));

        String chatColor = "";
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (String part : ObjectUtil.splitLine(pageLine, new ArrayList<>(List.of("<prev-page>", "<next-page>")))) {

            int pageNumber = page;
            String button = null;

            switch (part) {
                case "<prev-page>" -> {
                    pageNumber--;
                    button = locale.getFormatString("command." + commandName + ".prev-page", commandSender);
                }
                case "<next-page>" -> {
                    pageNumber++;
                    button = locale.getFormatString("command." + commandName + ".next-page", commandSender);
                }
            }

            FComponent component;
            if (button != null) {

                component = new FComponent(chatColor + button)
                        .addRunCommand("/" + commandName + " " + pageNumber);

            } else component = new FComponent(chatColor + part);

            componentBuilder.append(component.get(), ComponentBuilder.FormatRetention.NONE);

            chatColor = getLastColor(chatColor, componentBuilder);
        }

        return componentBuilder.create();
    }

    @NotNull
    public BaseComponent get() {
        return component;
    }

    public FComponent addHoverText(String showText) {
        return addHoverText(fromLegacyText(showText));
    }

    public FComponent addHoverText(BaseComponent[] baseComponents) {
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, baseComponents)
        );

        return this;
    }

    public FComponent addOpenURL(String url) {
        component.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL, url
        ));
        return this;
    }

    public FComponent addHoverItem(String itemJson) {
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)})
        );
        return this;
    }

    public FComponent addRunCommand(String command) {
        component.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, command)
        );
        return this;
    }

    public FComponent addSuggestCommand(String command) {
        component.setClickEvent(new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND, command)
        );
        return this;
    }

    private TextComponent componentFromLegacyText(String text) {
        return new TextComponent(fromLegacyText(text));
    }

    public enum Type {
        PLAYER_NAME,
        VOTE,
        TRANSLATABLE,
        TRANSLATABLE_ENTITY
    }

}
