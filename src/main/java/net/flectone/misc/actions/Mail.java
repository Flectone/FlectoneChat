package net.flectone.misc.actions;

import java.util.UUID;

public class Mail {

    private final String uuid;
    private final String sender;
    private final String receiver;
    private final String message;
    private boolean isRemoved = false;

    public Mail(String sender, String receiver, String message) {
        this.uuid = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getUUID() {
        return uuid;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

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
