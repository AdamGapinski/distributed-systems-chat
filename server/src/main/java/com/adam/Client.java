package com.adam;

public class Client {
    private final User user;
    private final Connection connection;

    public Client(User user, Connection connection) {
        this.user = user;
        this.connection = connection;
    }

    public User getUser() {
        return user;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean equalConnection(Client other) {
        return connection.equals(other.getConnection());
    }
}
