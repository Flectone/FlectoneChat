package net.flectone.misc.components;

import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FComponent {

    private final BaseComponent component;

    public FComponent(@NotNull String text) {
        this.component = componentFromLegacyText(text);
    }

    public FComponent() {
        this.component = new TextComponent();
    }

    public FComponent(@Nullable BaseComponent baseComponent) {
        this.component = baseComponent;
    }

    public FComponent(@NotNull FComponent fComponent) {
        this.component = fComponent.get();
    }

    @NotNull
    public BaseComponent get() {
        return component;
    }

    protected void set(@NotNull BaseComponent[] baseComponents) {
        this.component.setExtra(Arrays.asList(baseComponents));
    }

    @NotNull
    public static BaseComponent[] fromLegacyText(@NotNull String text) {
        return TextComponent.fromLegacyText(text);
    }

    @NotNull
    protected String getLastColor(@NotNull String lastColor, @NotNull ComponentBuilder componentBuilder) {
        return ChatColor.getLastColors(lastColor + componentBuilder.getCurrentComponent().toString());
    }

    @NotNull
    public FComponent addHoverText(@NotNull String showText) {
        return addHoverText(fromLegacyText(showText));
    }

    @NotNull
    public FComponent addHoverText(@NotNull BaseComponent[] baseComponents) {
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, baseComponents));
        return this;
    }

    @NotNull
    public FComponent addOpenURL(@NotNull String url) {
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    @NotNull
    public FComponent addHoverItem(@NotNull String itemJson) {
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)}));
        return this;
    }

    @NotNull
    public FComponent addRunCommand(@NotNull String command) {
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    @NotNull
    public FComponent addSuggestCommand(@NotNull String command) {
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return this;
    }

    @NotNull
    protected TextComponent componentFromLegacyText(@NotNull String text) {
        return new TextComponent(fromLegacyText(text));
    }
}
