package com.iwish.client;

import com.iwish.common.Actions;
import com.iwish.common.Request;
import com.iwish.common.Response;
import com.iwish.common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serializable;

public class LoginFrame extends JFrame {

    private final JTextField hostField = new JTextField(ClientConfig.serverHost());
    private final JTextField portField = new JTextField(String.valueOf(ClientConfig.serverPort()));
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel statusLabel = new JLabel(" ");
    private final RoundedButton loginBtn = new RoundedButton("Sign In", UiTheme.PRIMARY, Color.WHITE);

    public LoginFrame() {
        super("i-Wish");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(root);

        JLabel title = new JLabel("\uD83C\uDF81  i-Wish", SwingConstants.CENTER);
        title.setFont(UiTheme.FONT_TITLE);
        title.setForeground(UiTheme.PRIMARY_DARK);
        JLabel subtitle = new JLabel("Wish it. Share it. Get it together.", SwingConstants.CENTER);
        subtitle.setFont(UiTheme.FONT_SMALL);
        subtitle.setForeground(UiTheme.TEXT_MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(20));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(sectionLabel("Server address"));
        JPanel serverRow = new JPanel(new GridLayout(1, 2, 8, 0));
        serverRow.setOpaque(false);
        serverRow.setMaximumSize(new Dimension(2000, 32));
        serverRow.add(styledField(hostField));
        serverRow.add(styledField(portField));
        form.add(serverRow);
        form.add(Box.createVerticalStrut(16));

        form.add(sectionLabel("Username"));
        form.add(styledField(usernameField));
        form.add(Box.createVerticalStrut(12));

        form.add(sectionLabel("Password"));
        form.add(styledField(passwordField));
        form.add(Box.createVerticalStrut(18));

        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(2000, 44));
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(10));

        statusLabel.setForeground(UiTheme.WARN);
        statusLabel.setFont(UiTheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));

        JButton registerLink = new JButton("New here? Create an account");
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setForeground(UiTheme.PRIMARY_DARK);
        registerLink.setFont(UiTheme.FONT_SMALL);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        form.add(registerLink);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginBtn);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiTheme.FONT_SMALL);
        l.setForeground(UiTheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JComponent styledField(JTextField field) {
        field.setFont(UiTheme.FONT_BODY);
        field.setMaximumSize(new Dimension(2000, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCD6F7), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter a username and password.");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid port number.");
            return;
        }

        loginBtn.setEnabled(false);
        statusLabel.setForeground(UiTheme.TEXT_MUTED);
        statusLabel.setText("Connecting...");

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                try {
                    if (!NetworkClient.getInstance().isConnected()) {
                        NetworkClient.getInstance().connect(host, port);
                    }
                    Request req = new Request(Actions.LOGIN).put("username", username).put("password", password);
                    return NetworkClient.getInstance().send(req);
                } catch (Exception e) {
                    return Response.fail("Could not reach server at " + host + ":" + port + " (" + e.getMessage() + ")");
                }
            }

            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                try {
                    Response resp = get();
                    if (resp.isSuccess()) {
                        Session.setUser((User) resp.getData());
                        statusLabel.setForeground(UiTheme.ACCENT_GREEN);
                        statusLabel.setText(resp.getMessage());
                        new MainFrame().setVisible(true);
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
