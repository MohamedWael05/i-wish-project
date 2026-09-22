package com.iwish.client;

import com.iwish.common.Actions;
import com.iwish.common.Request;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private final NotificationsPanel notificationsPanel = new NotificationsPanel();
    private final FriendsPanel friendsPanel;
    private final RequestsPanel requestsPanel = new RequestsPanel();
    private final MyWishlistPanel myWishlistPanel = new MyWishlistPanel();

    private Timer pollTimer;

    public MainFrame() {
        super("i-Wish - " + Session.getUser().getFullName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        friendsPanel = new FriendsPanel(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);

        tabs.setFont(UiTheme.FONT_SUBHEAD);
        tabs.addTab("\uD83C\uDF81  My Wish List", myWishlistPanel);
        tabs.addTab("\uD83D\uDC65  Friends", friendsPanel);
        tabs.addTab("\uD83D\uDCE9  Requests", requestsPanel);
        tabs.addTab("\uD83D\uDD14  Notifications", notificationsPanel);
        root.add(tabs, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pollTimer != null) pollTimer.stop();
                NetworkClient.getInstance().disconnect();
            }
        });

        refreshAll();

        pollTimer = new Timer(8000, e -> {
            notificationsPanel.refresh();
            requestsPanel.refresh();
        });
        pollTimer.start();
    }

    public void refreshAll() {
        myWishlistPanel.refresh();
        friendsPanel.refresh();
        requestsPanel.refresh();
        notificationsPanel.refresh();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.PRIMARY);
        bar.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("\uD83C\uDF81  i-Wish");
        title.setFont(UiTheme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel("Hi, " + Session.getUser().getFullName() + "  ");
        user.setFont(UiTheme.FONT_BODY);
        user.setForeground(Color.WHITE);

        RoundedButton logoutBtn = new RoundedButton("Logout", UiTheme.ACCENT, Color.WHITE);
        logoutBtn.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(logoutBtn);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void logout() {
        ServerCall.run(this, new Request(Actions.LOGOUT), resp -> {
            NetworkClient.getInstance().disconnect();
            Session.setUser(null);
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}
