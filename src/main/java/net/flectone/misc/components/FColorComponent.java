package net.flectone.misc.components;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class FColorComponent extends FComponent{

    public FColorComponent(BaseComponent baseComponent, String color) {
        BaseComponent[] colorComponents = fromLegacyText(color);

        ComponentBuilder componentBuilder = new ComponentBuilder()
                .append(colorComponents)
                .append(baseComponent)
                .append(colorComponents);

        set(componentBuilder.create());
    }

    public FColorComponent(FComponent fComponent, String color) {
        this(fComponent.get(), color);
    }
}
