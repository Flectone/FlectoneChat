package net.flectone.misc.components;

import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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

    public FComponent(FComponent fComponent) {
        this.component = fComponent.get();
    }

    @NotNull
    public BaseComponent get() {
        return component;
    }

    protected void set(BaseComponent[] baseComponents) {
        this.component.setExtra(Arrays.asList(baseComponents));
    }

    public static BaseComponent[] fromLegacyText(String text) {
        return TextComponent.fromLegacyText(text);
    }

    protected String getLastColor(String lastColor, ComponentBuilder componentBuilder) {
        return ChatColor.getLastColors(lastColor + componentBuilder.getCurrentComponent().toString());
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

    protected TextComponent componentFromLegacyText(String text) {
        return new TextComponent(fromLegacyText(text));
    }
}
