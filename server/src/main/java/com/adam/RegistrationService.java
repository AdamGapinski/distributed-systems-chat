package com.adam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistrationService {

    private final MessageSender messageSender;

    private final ClientsConnectionManager clientsConnectionManager;

    @Autowired
    public RegistrationService(MessageSender messageSender, ClientsConnectionManager clientsConnectionManager) {
        this.messageSender = messageSender;
        this.clientsConnectionManager = clientsConnectionManager;
    }

    public void registerClient(String nick, Connection connection) {
        nick = nick.trim();
        if (clientsConnectionManager.getClientByNick(nick).isPresent()) {
            messageSender.sendRegistrationRejection("Nick is already taken", connection);
        } else if (nick.trim().equals("")) {
            messageSender.sendRegistrationRejection("Nick is invalid",connection);
        } else {
            Client client = new Client(new User(nick), connection);
            messageSender.sendMessageFromServer(String.format("User %s has joined", nick));
            messageSender.sendRegistrationAcceptance(String.format("You are registered with nick %s", nick), connection);
            clientsConnectionManager.addClient(client);
        }
    }
}
