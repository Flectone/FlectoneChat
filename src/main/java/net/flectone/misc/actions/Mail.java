package net.flectone.misc.actions;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Mail {

    private final UUID uuid;
    private final UUID sender;
    private final UUID receiver;
    private final String message;
    private boolean isRemoved = false;

    public Mail(@NotNull UUID sender, @NotNull UUID receiver, @NotNull String message) {
        this.uuid = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public UUID getSender() {
        return sender;
    }

    @NotNull
    public UUID getReceiver() {
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
