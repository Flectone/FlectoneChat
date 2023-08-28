package net.flectone.misc.entity.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerMail {

    private final UUID uuid;
    private final UUID sender;
    private final UUID receiver;
    private final String message;

    public PlayerMail(@NotNull UUID uuid, @NotNull UUID sender, @NotNull UUID receiver, @NotNull String message) {
        this.uuid = uuid;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public PlayerMail(@NotNull UUID sender, @NotNull UUID receiver, @NotNull String message) {
        this(UUID.randomUUID(), sender, receiver, message);
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
}
