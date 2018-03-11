package com.adam;

public class Message {
    private String value;
    private MessageType messageType;
    private User from;

    public Message(String value) {
        this.value = value;
    }

    public Message() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Message{" +
                "value='" + value + '\'' +
                ", messageType=" + messageType +
                ", from=" + from +
                '}';
    }
}
