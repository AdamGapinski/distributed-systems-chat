package com.adam;

public class Client {
    private final User user;
    private Connection connection;
    private final ClientSocketInfo clientSocketInfo;

    public Client(User user, Connection connection, ClientSocketInfo clientSocketInfo) {
        this.user = user;
        this.connection = connection;
        this.clientSocketInfo = clientSocketInfo;
    }

    public boolean equalConnection(Client other) {
        return connection.equals(other.getConnection());
    }

    public boolean equalDatagramSocket(Client other) {
        return clientSocketInfo.equals(other.getClientSocketInfo());
    }

    public User getUser() {
        return user;
    }

    public Connection getConnection() {
        return connection;
    }

    public ClientSocketInfo getClientSocketInfo() {
        return clientSocketInfo;
    }
}
