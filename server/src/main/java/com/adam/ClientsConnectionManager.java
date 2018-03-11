package com.adam;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ClientsConnectionManager {

    private Map<Connection, Client> connectionClientMap = new ConcurrentHashMap<>();

    public void addClient(Client client) {
        connectionClientMap.put(client.getConnection(), client);
    }

    public void removeConnection(Connection connection) {
        connectionClientMap.remove(connection);
    }

    public Optional<Client> getClientByNick(String nick) {
        return connectionClientMap.values()
                .stream()
                .filter(client -> client.getUser().getNick().equals(nick))
                .findAny();
    }

    public Optional<Client> getClientByConnection(Connection connection) {
        return Optional.ofNullable(connectionClientMap.get(connection));
    }

    public Collection<Connection> getAllConnections() {
        return new ArrayList<>(connectionClientMap.keySet());
    }

    public Collection<Connection> getConnectionsExceptClient(Client except) {
        return connectionClientMap.entrySet()
                .stream()
                .filter(connectionClientEntry -> !connectionClientEntry.getValue().equalConnection(except))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
