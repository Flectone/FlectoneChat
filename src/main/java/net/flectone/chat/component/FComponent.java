package net.flectone.chat.component;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.model.file.FConfiguration;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FComponent {

    private final BaseComponent component;
    protected final FConfiguration locale;
    protected final FConfiguration config;

    public FComponent(@Nullable BaseComponent baseComponent) {
        this.component = baseComponent;

        FlectoneChat plugin = FlectoneChat.getPlugin();
        locale = plugin.getFileManager().getLocale();
        config = plugin.getFileManager().getConfig();
    }

    public FComponent(@NotNull String text) {
        this(new TextComponent(fromLegacyText(text)));
    }

    public FComponent() {
        this(new TextComponent());
    }

    public FComponent(@NotNull FComponent fComponent) {
        this(fComponent.get());
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
}
