package com.adam;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConnectionEventHandler {

    private final RegistrationService registrationService;

    private final MessageSender messageSender;

    private final ClientsConnectionManager clientsConnectionManager;

    private final Logger logger;

    @Autowired
    public ConnectionEventHandler(RegistrationService registrationService,
                                  MessageSender messageSender,
                                  ClientsConnectionManager clientsConnectionManager, Logger logger) {
        this.registrationService = registrationService;
        this.messageSender = messageSender;
        this.clientsConnectionManager = clientsConnectionManager;
        this.logger = logger;
    }

    public void handleMessage(Connection connection, Message message) {
        logger.debug("Received: {}", message);
        switch (message.getMessageType()) {
            case REGISTRATION_REQUEST:
                registrationService.registerClient(message.getValue(), connection);
                break;
            case TEXT:
                Optional<Client> clientOptional = clientsConnectionManager.getClientByConnection(connection);
                if (clientOptional.isPresent()) {
                    Client client = clientOptional.get();
                    messageSender.sendMessageFrom(client, message.getValue());
                } else {
                    messageSender.sendUserNotRegistered("User is not registered", connection);
                }
                break;
        }
    }

    public void handleClose(Connection connection) {
        clientsConnectionManager.getClientByConnection(connection).ifPresent(client -> {
            clientsConnectionManager.removeConnection(connection);
            messageSender.sendMessageFromServer(String.format("User %s has left", client.getUser().getNick()));
        });
    }
}
