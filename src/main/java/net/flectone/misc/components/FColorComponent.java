package net.flectone.misc.components;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

public class FColorComponent extends FComponent{

    public FColorComponent(@NotNull BaseComponent baseComponent, @NotNull String color) {
        BaseComponent[] colorComponents = fromLegacyText(color);

        ComponentBuilder componentBuilder = new ComponentBuilder()
                .append(colorComponents)
                .append(baseComponent)
                .append(colorComponents);

        set(componentBuilder.create());
    }

    public FColorComponent(@NotNull FComponent fComponent, @NotNull String color) {
        this(fComponent.get(), color);
    }
}
