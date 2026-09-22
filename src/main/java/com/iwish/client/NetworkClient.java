package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private NetworkClient() {}

    public static synchronized NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    public synchronized void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized Response send(Request request) throws IOException, ClassNotFoundException {
        if (!isConnected()) {
            throw new IOException("Not connected to server.");
        }
        out.reset();
        out.writeObject(request);
        out.flush();
        Object obj = in.readObject();
        if (obj instanceof Response) {
            return (Response) obj;
        }
        throw new IOException("Unexpected response from server.");
    }

    public synchronized void disconnect() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
