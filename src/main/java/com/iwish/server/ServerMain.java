package com.iwish.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

public class ServerMain {

    private static ServerSocket serverSocket;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("           i-Wish  SERVER");
        System.out.println("=========================================");

        try (Connection c = Database.getConnection()) {
            System.out.println("[Server] Connected to MySQL database '" + DBConfig.dbName() + "' successfully.");
        } catch (Exception e) {
            System.err.println("[Server] Could NOT connect to MySQL: " + e.getMessage());
            System.err.println("[Server] Check config.properties (db.host/db.port/db.name/db.user/db.password)");
            System.err.println("[Server] and make sure you ran db/iwish_schema.sql. Exiting.");
            return;
        }

        int port = DBConfig.serverPort();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("[Server] Could not bind to port " + port + ": " + e.getMessage());
            return;
        }

        System.out.println("[Server] Listening on port " + port);
        System.out.println("[Server] Type 'stop' + Enter at any time to shut the server down.\n");

        Thread consoleThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("stop")) {
                        stopServer();
                        break;
                    }
                }
            } catch (IOException ignored) {}
        });
        consoleThread.setDaemon(true);
        consoleThread.start();

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.setDaemon(true);
                t.start();
            } catch (IOException e) {
                if (running) {
                    System.out.println("[Server] Accept error: " + e.getMessage());
                }
            }
        }
        System.out.println("[Server] Stopped.");
    }

    private static void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }
}
