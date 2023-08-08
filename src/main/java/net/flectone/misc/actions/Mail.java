package net.flectone.misc.actions;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Mail {

    private final String uuid;
    private final String sender;
    private final String receiver;
    private final String message;
    private boolean isRemoved = false;

    public Mail(@NotNull String sender, @NotNull String receiver, @NotNull String message) {
        this.uuid = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    @NotNull
    public String getUUID() {
        return uuid;
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getReceiver() {
        return receiver;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }
}
