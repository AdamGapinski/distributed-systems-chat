package com.adam;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

@Component
public class DatagramListener {

    private final DatagramSocket datagramSocket;

    private final DatagramMessageHandler messageHandler;

    private final Logger logger;

    private final Scheduler scheduler = new Scheduler();
    private final Gson gson = new Gson();
    private final int receiveBufferSize = 8192;

    @Autowired
    public DatagramListener(DatagramSocket datagramSocket, DatagramMessageHandler messageHandler, Logger logger) {
        this.datagramSocket = datagramSocket;
        this.messageHandler = messageHandler;
        this.logger = logger;
    }

    public void start() throws SocketException {
        scheduler.schedule(this::loop);
        logger.debug("Datagram listener started");
    }

    private void loop() {
        byte[] receiveBuffer = new byte[receiveBufferSize];
        DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        while(true) {
            byte blank = -1;
            Arrays.fill(receiveBuffer, blank);
            try {
                datagramSocket.receive(datagramPacket);
                logger.debug("Received datagram packet {}", datagramPacket);
                int dataLength = getDataLength(datagramPacket.getData(), blank);
                String json = new String(datagramPacket.getData(), 0, dataLength);
                scheduler.schedule(() -> {
                    Message message = gson.fromJson(json, Message.class);
                    messageHandler.handleMessage(message);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getDataLength(byte[] data, byte endValue) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == endValue) {
                return i;
            }
        }
        return data.length;
    }
}