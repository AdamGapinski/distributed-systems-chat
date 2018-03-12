package com.adam;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection {
    private final ConnectionEventHandler handler;
    private final Socket socket;
    private final Scanner input;
    private final PrintWriter output;
    private final Logger logger;

    private final Scheduler scheduler = new Scheduler();
    private final Gson gson = new Gson();

    public Connection(Socket socket, ConnectionEventHandler handler, Logger logger) throws IOException {
        this.socket = socket;
        this.handler = handler;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(this.socket.getOutputStream(), true);
        this.logger = logger;
    }

    public void sendMessage(Message message) {
        String json = gson.toJson(message);
        output.println(json);
    }

    public void listen() {
        scheduler.schedule(() -> {
            try {
                while (input.hasNext()) {
                    String json = input.nextLine();
                    Message message = gson.fromJson(json, Message.class);
                    handler.handleMessage(this, message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
        });
    }

    private void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scheduler.schedule(() -> handler.handleClose(this));
    }
}
