package com.iwish.client;

import com.iwish.common.Actions;
import com.iwish.common.Request;
import com.iwish.common.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final JTextField usernameField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");

    public RegisterDialog(JFrame owner) {
        super(owner, "Create your i-Wish account", true);
        setSize(400, 480);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel();
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 30, 24, 30));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        setContentPane(root);

        JLabel title = new JLabel("Join i-Wish \uD83C\uDF89");
        title.setFont(UiTheme.FONT_HEADER);
        title.setForeground(UiTheme.PRIMARY_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(16));

        root.add(field("Username", usernameField));
        root.add(field("Full name", fullNameField));
        root.add(field("Email", emailField));
        root.add(field("Password", passwordField));
        root.add(field("Confirm password", confirmField));

        RoundedButton createBtn = new RoundedButton("Create Account", UiTheme.ACCENT_GREEN, Color.WHITE);
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(2000, 42));
        createBtn.addActionListener(e -> doRegister());
        root.add(Box.createVerticalStrut(8));
        root.add(createBtn);
        root.add(Box.createVerticalStrut(8));

        statusLabel.setFont(UiTheme.FONT_SMALL);
        statusLabel.setForeground(UiTheme.WARN);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(statusLabel);

        getRootPane().setDefaultButton(createBtn);
    }

    private JPanel field(String label, JTextField tf) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UiTheme.FONT_SMALL);
        l.setForeground(UiTheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setFont(UiTheme.FONT_BODY);
        tf.setMaximumSize(new Dimension(2000, 32));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCD6F7), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(tf);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username, full name and password are required.");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        String host = ClientConfig.serverHost();
        int port = ClientConfig.serverPort();

        statusLabel.setForeground(UiTheme.TEXT_MUTED);
        statusLabel.setText("Creating your account...");

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                try {
                    if (!NetworkClient.getInstance().isConnected()) {
                        NetworkClient.getInstance().connect(host, port);
                    }
                    Request req = new Request(Actions.REGISTER)
                            .put("username", username)
                            .put("password", password)
                            .put("fullName", fullName)
                            .put("email", email);
                    return NetworkClient.getInstance().send(req);
                } catch (Exception e) {
                    return Response.fail("Could not reach server: " + e.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    Response resp = get();
                    if (resp.isSuccess()) {
                        statusLabel.setForeground(UiTheme.ACCENT_GREEN);
                        statusLabel.setText(resp.getMessage());
                        JOptionPane.showMessageDialog(RegisterDialog.this,
                                "Account created! You can now sign in.", "i-Wish", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        statusLabel.setForeground(UiTheme.WARN);
                        statusLabel.setText(resp.getMessage());
                    }
                } catch (Exception e) {
                    statusLabel.setForeground(UiTheme.WARN);
                    statusLabel.setText("Unexpected error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
