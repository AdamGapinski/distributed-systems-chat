package com.adam;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class ConnectionListener {

    private final ConnectionEventHandler connectionEventHandler;

    private final Logger logger;

    private ServerSocket socket;
    private final Scheduler scheduler = new Scheduler();

    private final int port = 9024;

    @Autowired
    public ConnectionListener(ConnectionEventHandler connectionEventHandler, Logger logger) {
        this.connectionEventHandler = connectionEventHandler;
        this.logger = logger;
    }

    public void start() throws IOException {
        socket = new ServerSocket(port);
        scheduler.schedule(this::loop);
        logger.debug("Connection listener started");
    }

    private void loop() {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                logger.debug("Accepted: {}", clientSocket);
                Connection connection = new Connection(clientSocket, connectionEventHandler, logger);
                connection.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
