package com.adam;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class ClientMain {

    private static final Logger logger = LogManager.getLogger();
    private static final int serverPort = 9024;
    private static final int clientMulticastPort = 9025;
    private static final int datagramReceiveBufferSize = 8192;
    private static final String serverHost = "localhost";
    private static final String multicastAddress = "228.5.6.7";
    private static String nick;
    private static Socket serverSocket;
    private static DatagramSocket datagramSocket;
    private static MulticastSocket multicastSocket;

    public static void main(String[] args) throws IOException, InterruptedException {
        serverSocket = new Socket("localhost", serverPort);
        datagramSocket = new DatagramSocket();
        multicastSocket = new MulticastSocket(clientMulticastPort);
        multicastSocket.joinGroup(InetAddress.getByName(multicastAddress));
        logger.info("Client started");

        nick = getNick(args);
        Thread listenThread = listenAsync();
        Thread listenDatagramThread = listenDatagramAsync();
        Thread listenMulticastThread = listenMulticastAsync();
        Thread registrationThread = requestRegisterAsync();

        registrationThread.join();
        listenThread.join();
        listenDatagramThread.join();
        listenMulticastThread.join();
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
                    nick = nickPrompt("Please, choose your nick: ");
                    requestRegisterAsync();
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

    private static Thread listenDatagramAsync() {
        Thread thread = new Thread(() -> {
            try {
                listenDatagram();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        logger.debug("Started listening");
        return thread;
    }

    private static void listenDatagram() throws IOException {
        byte[] receiveBuffer = new byte[datagramReceiveBufferSize];
        Gson gson = new Gson();
        while (true) {
            byte blank = -1;
            Arrays.fill(receiveBuffer, blank);
            DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            datagramSocket.receive(datagramPacket);
            int dataLength = getDataLength(datagramPacket.getData(), blank);
            String json = new String(datagramPacket.getData(), 0, dataLength);
            Message message = gson.fromJson(json, Message.class);
            System.out.printf("(UDP) %s says: %s\n", message.getFrom().getNick(), message.getValue());
        }
    }

    private static Thread listenMulticastAsync() {
        Thread thread = new Thread(() -> {
            try {
                listenMulticast();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        logger.debug("Started listening for multicast");
        return thread;
    }

    private static void listenMulticast() throws IOException {
        byte[] receiveBuffer = new byte[datagramReceiveBufferSize];
        Gson gson = new Gson();
        while (true) {
            byte blank = -1;
            Arrays.fill(receiveBuffer, blank);
            DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            multicastSocket.receive(datagramPacket);
            int dataLength = getDataLength(datagramPacket.getData(), blank);
            String json = new String(datagramPacket.getData(), 0, dataLength);
            Message message = gson.fromJson(json, Message.class);
            if (!message.getFrom().getNick().equals(nick)) {
                System.out.printf("(Multicast) %s says: %s\n", message.getFrom().getNick(), message.getValue());
            }
        }
    }

    private static int getDataLength(byte[] data, byte endValue) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == endValue) {
                return i;
            }
        }
        return data.length;
    }

    private static Thread requestRegisterAsync() {
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
        message.setClientSocketInfo(new ClientSocketInfo(datagramSocket.getLocalAddress().getHostAddress(),
                datagramSocket.getLocalPort()));
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
            String lineSeparator = System.getProperty("line.separator");
            String doubleLineSeparator = lineSeparator + lineSeparator;
            String input = getConsoleInput(scanner, lineSeparator);
            switch (input.trim()) {
                case "U": {
                    input = getConsoleInput(scanner, doubleLineSeparator);
                    Message message = new Message(input);
                    message.setMessageType(MessageType.TEXT);
                    message.setFrom(new User(nick));
                    String json = gson.toJson(message);
                    byte[] datagramBuffer = json.getBytes();
                    InetAddress serverAddress = InetAddress.getByName(serverHost);
                    DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length, serverAddress, serverPort);
                    datagramSocket.send(datagramPacket);
                    logger.debug("Sent by UDP: {}", message);
                    break;
                }
                case "M": {
                    input = getConsoleInput(scanner, doubleLineSeparator);
                    Message message = new Message(input);
                    message.setMessageType(MessageType.TEXT);
                    message.setFrom(new User(nick));
                    String json = gson.toJson(message);
                    byte[] datagramBuffer = json.getBytes();
                    InetAddress serverAddress = InetAddress.getByName(multicastAddress);
                    DatagramPacket datagramPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length, serverAddress, clientMulticastPort);
                    datagramSocket.send(datagramPacket);
                    logger.debug("Sent by multicast: {}", message);
                    break;
                }
                default: {
                    Message message = new Message(input);
                    message.setMessageType(MessageType.TEXT);
                    message.setFrom(new User(nick));
                    String json = gson.toJson(message);
                    socketWriter.println(json);
                    logger.debug("Sent by TCP: {}", message);
                    break;
                }
            }
        }
    }

    private static String getConsoleInput(Scanner scanner, String delimiter) {
        while (true) {
            scanner.useDelimiter(delimiter);
            String line = scanner.next();
            if (!line.trim().equals("")) {
                return line;
            }
        }
    }
}
