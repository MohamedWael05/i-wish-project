package com.iwish.server;

import com.iwish.common.Request;
import com.iwish.common.Response;
import com.iwish.common.User;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher dispatcher = new RequestDispatcher();
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("[Server] Client connected: " + remote);
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException | java.net.SocketException e) {
                    break;
                }
                if (!(obj instanceof Request)) continue;
                Request req = (Request) obj;

                Response resp = dispatcher.handle(req, this);
                out.reset();
                out.writeObject(resp);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Server] Connection error with " + remote + ": " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("[Server] Client disconnected: " + remote
                    + (currentUser != null ? " (" + currentUser.getUsername() + ")" : ""));
        }
    }
}
