package com.adam;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    private static final Logger logger = LogManager.getLogger();

    private static Socket serverSocket;

    public static void main(String[] args) throws IOException, InterruptedException {
        serverSocket = new Socket("localhost", 9024);
        logger.info("Client started");
        logger.debug("Socket {}", serverSocket);

        String nick = getNick(args);
        Thread listenThread = listenAsync();
        Thread registrationThread = requestRegisterAsync(nick);

        registrationThread.join();
        listenThread.join();
    }

    private static String getNick(String[] args) {
        String nick;
        if (args.length > 0) {
            nick = args[0];
        } else {
            nick = nickPrompt("Please, choose your nick: ");
        }
        return nick;
    }

    private static String nickPrompt(String prompt) {
        Scanner scanner = new Scanner(System.in);
        String nick = "";
        while (nick.equals("")) {
            System.out.println(prompt);
            nick = scanner.nextLine().trim();
            if (nick.equals("")) {
                System.out.println("Nick is invalid");
            }
        }
        return nick;
    }

    private static Thread listenAsync() {
        Thread thread = new Thread(() -> {
            try {
                listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        logger.debug("Started listening");
        return thread;
    }

    private static void listen() throws IOException {
        Gson gson = new Gson();
        Scanner input = new Scanner(serverSocket.getInputStream());
        while (input.hasNext()) {
            String json = input.nextLine();
            Message message = gson.fromJson(json, Message.class);
            logger.debug("Received: {}", message);

            switch (message.getMessageType()) {
                case REGISTRATION_ACCEPTED:
                    System.out.printf("server info: %s\n", message.getValue());
                    writeAsync();
                    break;
                case REGISTRATION_REJECTED:
                    System.out.printf("server info: %s\n", message.getValue());
                    requestRegisterAsync(nickPrompt("Please, choose your nick: "));
                    break;
                case USER_NOT_REGISTERED:
                    System.out.printf("server info: %s\n", message.getValue());
                    break;
                case TEXT:
                    User from = message.getFrom();
                    if (from != null) {
                        System.out.printf("%s says: %s\n", message.getFrom().getNick(), message.getValue());
                    } else {
                        System.out.printf("server info: %s\n", message.getValue());
                    }
                    break;
            }
        }
        logger.info("Lost connection with server");
    }

    private static Thread requestRegisterAsync(String nick) {
        Thread thread = new Thread(() -> {
            try {
                requestRegister(nick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        logger.debug("Started requesting registration");
        return thread;
    }

    private static void requestRegister(String nick) throws IOException {
        PrintWriter socketWriter = new PrintWriter(serverSocket.getOutputStream(), true);
        Message message = new Message(nick);
        message.setMessageType(MessageType.REGISTRATION_REQUEST);
        Gson gson = new Gson();
        String json = gson.toJson(message);
        socketWriter.println(json);
        logger.debug("Sent: {}", message);
    }

    private static Thread writeAsync() {
        Thread thread = new Thread(() -> {
            try {
                write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        logger.debug("Started writing");
        return thread;
    }

    private static void write() throws IOException {
        Scanner scanner = new Scanner(System.in);
        PrintWriter socketWriter = new PrintWriter(serverSocket.getOutputStream(), true);
        Gson gson = new Gson();

        while (true) {
            String messageString = scanner.nextLine();
            Message message = new Message(messageString);
            message.setMessageType(MessageType.TEXT);
            String json = gson.toJson(message);
            logger.debug("Sent: {}", message);
            socketWriter.println(json);
        }
    }

}
