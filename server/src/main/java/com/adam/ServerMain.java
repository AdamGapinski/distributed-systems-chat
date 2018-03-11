package com.adam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@ComponentScan
@Configuration
public class ServerMain {

    @Bean
    Logger logger() {
        return LogManager.getLogger();
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ServerMain.class);
        ConnectionListener listener = context.getBean(ConnectionListener.class);
        Logger logger = context.getBean(Logger.class);

        listener.start();
        logger.info("Server started");
    }
}
