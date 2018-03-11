package com.adam;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    private final ClientsConnectionManager clientsConnectionManager;

    private final Logger logger;

    @Autowired
    public MessageSender(ClientsConnectionManager clientsConnectionManager, Logger logger) {
        this.clientsConnectionManager = clientsConnectionManager;
        this.logger = logger;
    }

    public void sendRegistrationRejection(String messageString, Connection connection) {
        singleConnectionSend(messageString, MessageType.REGISTRATION_REJECTED, connection);
    }

    public void sendRegistrationAcceptance(String messageString, Connection connection) {
        singleConnectionSend(messageString, MessageType.REGISTRATION_ACCEPTED, connection);
    }

    public void sendUserNotRegistered(String messageString, Connection connection) {
        singleConnectionSend(messageString, MessageType.USER_NOT_REGISTERED, connection);
    }

    private void singleConnectionSend(String messageString, MessageType messageType, Connection connection) {
        Message message = new Message(messageString);
        message.setMessageType(messageType);
        connection.sendMessage(message);
        logger.debug("Sent: {}", message);
    }

    public void sendMessageFromServer(String messageString) {
        Message message = new Message(messageString);
        clientsConnectionManager.getAllConnections().forEach(connection -> {
            message.setMessageType(MessageType.TEXT);
            connection.sendMessage(message);
        });
        logger.debug("Sent: {}", message);
    }

    public void sendMessageFrom(Client client, String messageString) {
        Message message = new Message(messageString);
        clientsConnectionManager.getConnectionsExceptClient(client).forEach(connection -> {
            message.setMessageType(MessageType.TEXT);
            message.setFrom(client.getUser());
            connection.sendMessage(message);
        });
        logger.debug("Sent: {}", message);
    }
}
