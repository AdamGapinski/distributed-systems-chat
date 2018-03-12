package com.adam;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DatagramMessageHandler {

    private final ClientsManager clientsManager;

    private final DatagramMessageSender messageSender;

    private final Logger logger;

    @Autowired
    public DatagramMessageHandler(ClientsManager clientsManager, DatagramMessageSender messageSender, Logger logger) {
        this.clientsManager = clientsManager;
        this.messageSender = messageSender;
        this.logger = logger;
    }

    public void handleMessage(Message message) {
        logger.debug("Received: {}", message);
        MessageType messageType = message.getMessageType();
        User from = message.getFrom();

        if (messageType.equals(MessageType.TEXT) && from != null) {
            String nick = from.getNick();
            Optional<Client> clientOptional = clientsManager.getClientByNick(nick);
            if (clientOptional.isPresent()) {
                Client client = clientOptional.get();
                messageSender.sendMessageFrom(client, message.getValue());
            } else {
                messageSender.sendUserNotRegistered("User is not registered",
                        message.getClientSocketInfo());
            }
        }
    }
}
