package net.flectone.chat.module;

import lombok.Getter;

public abstract class FInfo implements FAction {

    @Getter
    private FModule module;

    public FInfo(FModule module) {
        this.module = module;
    }
}