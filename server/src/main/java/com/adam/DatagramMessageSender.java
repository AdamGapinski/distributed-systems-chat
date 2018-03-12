package com.adam;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Component
public class DatagramMessageSender {

    private final ClientsManager clientsManager;
    private final Logger logger;
    private final DatagramSocket datagramSocket;
    private final Gson gson = new Gson();

    private final int retries = 3;

    @Autowired
    public DatagramMessageSender(ClientsManager clientsManager, Logger logger, DatagramSocket datagramSocket) {
        this.clientsManager = clientsManager;
        this.logger = logger;
        this.datagramSocket = datagramSocket;
    }

    public void sendUserNotRegistered(String messageString, ClientSocketInfo clientSocketInfo) {
        Message message = new Message(messageString);
        message.setMessageType(MessageType.USER_NOT_REGISTERED);
        trySendingMessage(clientSocketInfo, message, retries);
        logger.debug("Sent: {}", message);
    }

    public void sendMessageFrom(Client client, String messageString) {
        Message message = new Message(messageString);
        message.setMessageType(MessageType.TEXT);
        message.setFrom(client.getUser());
        clientsManager.getDatagramSocketsExceptClient(client)
                .forEach(clientDatagramSocket -> trySendingMessage(clientDatagramSocket, message, retries));
        logger.debug("Sent: {}", message);
    }

    private void trySendingMessage(ClientSocketInfo clientSocketInfo, Message message, int retries) {
        boolean sent = false;
        while (!sent && retries > 0) {
            try {
                String json = gson.toJson(message);
                byte[] responseBuffer = json.getBytes();
                InetAddress inetAddress = InetAddress.getByName(clientSocketInfo.getDatagramHostAddress());
                DatagramPacket datagramPacket = new DatagramPacket(responseBuffer,
                        responseBuffer.length,
                        inetAddress,
                        clientSocketInfo.getDatagramPortNumber());
                datagramSocket.send(datagramPacket);
                sent = true;
            } catch (IOException e) {
                e.printStackTrace();
                retries--;
                logger.info("{} retries left", retries);
            }
        }
    }
}
