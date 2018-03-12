package com.adam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

@ComponentScan
@Configuration
public class ServerMain {

    public static void main(String[] args) throws IOException {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ServerMain.class);
        ConnectionListener connectionListener = context.getBean(ConnectionListener.class);
        DatagramListener datagramListener = context.getBean(DatagramListener.class);
        Logger logger = context.getBean(Logger.class);

        connectionListener.start();
        datagramListener.start();
        logger.info("Server started");
    }

    @Bean
    Logger logger() {
        return LogManager.getLogger();
    }

    @Bean
    DatagramSocket datagramSocket() throws SocketException {
        int port = 9024;
        return new DatagramSocket(port);
    }
}
