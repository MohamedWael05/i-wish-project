package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;

import javax.swing.*;
import java.awt.*;

public final class ServerCall {

    public interface Callback {
        void onResult(Response response);
    }

    private ServerCall() {}

    public static void run(Component parent, Request request, Callback callback) {
        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                try {
                    return NetworkClient.getInstance().send(request);
                } catch (Exception e) {
                    return Response.fail("Connection lost: " + e.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    callback.onResult(get());
                } catch (Exception e) {
                    callback.onResult(Response.fail("Unexpected error: " + e.getMessage()));
                }
            }
        };
        worker.execute();
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "i-Wish", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "i-Wish", JOptionPane.INFORMATION_MESSAGE);
    }
}
