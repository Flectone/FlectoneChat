package net.flectone.chat.component;

import lombok.Getter;
import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FComponent {

    private BaseComponent component;
    protected final FConfiguration locale;
    protected final FConfiguration config;
    @Getter
    private HoverEvent hoverEvent;
    @Getter
    private ClickEvent clickEvent;

    public FComponent(@Nullable BaseComponent baseComponent) {
        this.component = baseComponent;

        FlectoneChat plugin = FlectoneChat.getPlugin();
        locale = plugin.getFileManager().getLocale();
        config = plugin.getFileManager().getConfig();
    }

    public FComponent(@NotNull String text) {
        this(new TextComponent(fromLegacyText(text)));
    }

    public void set(@NotNull String text) {
        component = new TextComponent(fromLegacyText(text));
        component.setClickEvent(clickEvent);
        component.setHoverEvent(hoverEvent);
    }

    public FComponent() {
        this(new TextComponent());
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
    public FComponent addHoverText(@NotNull String showText) {
        return addHoverText(fromLegacyText(showText));
    }

    @NotNull
    public FComponent addHoverText(@NotNull BaseComponent[] baseComponents) {
        setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, baseComponents));
        return this;
    }

    @NotNull
    public FComponent addOpenURL(@NotNull String url) {
        setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    @NotNull
    public FComponent addHoverItem(@NotNull String itemJson) {
        setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)}));
        return this;
    }

    @NotNull
    public FComponent addRunCommand(@NotNull String command) {
        setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    @NotNull
    public FComponent addSuggestCommand(@NotNull String command) {
        setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return this;
    }

    public void setHoverEvent(@NotNull HoverEvent hoverEvent) {
        component.setHoverEvent(hoverEvent);
        this.hoverEvent = hoverEvent;
    }

    public void setClickEvent(@NotNull ClickEvent clickEvent) {
        component.setClickEvent(clickEvent);
        this.clickEvent = clickEvent;
    }
}
