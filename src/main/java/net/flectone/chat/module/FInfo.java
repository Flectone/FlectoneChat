package net.flectone.chat.module;

import lombok.Getter;

@Getter
public abstract class FInfo implements FAction {

    private final FModule module;

    public FInfo(FModule module) {
        this.module = module;
    }
}