package com.adam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistrationService {

    private final MessageSender messageSender;

    private final ClientsManager clientsManager;

    @Autowired
    public RegistrationService(MessageSender messageSender, ClientsManager clientsManager) {
        this.messageSender = messageSender;
        this.clientsManager = clientsManager;
    }

    public void registerClient(String nick, Connection connection, ClientSocketInfo clientSocketInfo) {
        nick = nick.trim();
        if (clientsManager.getClientByNick(nick).isPresent()) {
            messageSender.sendRegistrationRejection("Nick is already taken", connection);
        } else if (nick.trim().equals("")) {
            messageSender.sendRegistrationRejection("Nick is invalid", connection);
        } else if (clientSocketInfo == null ||
                clientSocketInfo.getDatagramPortNumber() == null ||
                clientSocketInfo.getDatagramHostAddress() == null) {
            messageSender.sendRegistrationRejection("Datagram socket info is invalid", connection);
        } else {
            Client client = new Client(new User(nick), connection, clientSocketInfo);
            messageSender.sendMessageFromServer(String.format("User %s has joined", nick));
            messageSender.sendRegistrationAcceptance(String.format("You are registered with nick %s", nick), connection);
            clientsManager.addClient(client);
        }
    }
}
