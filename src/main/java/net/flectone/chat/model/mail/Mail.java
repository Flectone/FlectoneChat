package net.flectone.chat.model.mail;

import lombok.Getter;

@Getter
public class Mail {

    private int id;
    private final String sender;
    private final String receiver;
    private final String message;

    public Mail(int id, String sender, String receiver, String message) {
        this(sender, receiver, message);
        this.id = id;
    }

    public Mail(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

}
