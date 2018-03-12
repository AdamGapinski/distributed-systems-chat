package com.adam;

public class Message {
    private String value;
    private MessageType messageType;
    private User from;
    private ClientSocketInfo clientSocketInfo;

    public Message(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
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

    public ClientSocketInfo getClientSocketInfo() {
        return clientSocketInfo;
    }

    public void setClientSocketInfo(ClientSocketInfo clientSocketInfo) {
        this.clientSocketInfo = clientSocketInfo;
    }
}
